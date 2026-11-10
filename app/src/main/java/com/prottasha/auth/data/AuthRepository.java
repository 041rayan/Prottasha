package com.prottasha.auth.data;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prottasha.auth.domain.AuthState;
import com.prottasha.auth.domain.PasswordPolicy;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

public final class AuthRepository {

    private final AuthGateway gateway;
    private final MutableLiveData<AuthState> state = new MutableLiveData<>();
    private AuthState currentState;
    private long generation;

    public AuthRepository(AuthGateway gateway) {
        this.gateway = gateway;
        bootstrap();
    }

    public LiveData<AuthState> observeState() {
        return state;
    }

    public AuthState currentState() {
        return currentState;
    }

    public void createAccount(String displayName, String email, String password) {
        if (PasswordPolicy.validate(password) != PasswordPolicy.Result.VALID) {
            publish(AuthState.of(AuthState.Status.ERROR, null,
                    AuthState.MessageCode.WEAK_PASSWORD));
            return;
        }
        long operation = beginLoading(null);
        gateway.createAccount(displayName, email, password, result -> {
            if (!isCurrent(operation)) {
                return;
            }
            if (!result.isSuccess()) {
                publishFailure(result, null);
                return;
            }
            SessionUser user = result.valueOrNull();
            publish(AuthState.of(AuthState.Status.UNVERIFIED, user));
            gateway.sendVerification(sent -> {
                if (!isCurrent(operation)) {
                    return;
                }
                if (sent.isSuccess()) {
                    publish(AuthState.of(AuthState.Status.UNVERIFIED, user,
                            AuthState.MessageCode.VERIFICATION_SENT));
                } else {
                    publish(AuthState.of(AuthState.Status.UNVERIFIED, user,
                            AuthState.MessageCode.VERIFICATION_SEND_FAILED));
                }
            });
        });
    }

    public void signIn(String email, String password) {
        long operation = beginLoading(null);
        gateway.signIn(email, password, result -> {
            if (!isCurrent(operation)) {
                return;
            }
            if (!result.isSuccess()) {
                publishFailure(result, null);
                return;
            }
            reloadForRouting(operation, result.valueOrNull());
        });
    }

    public void sendVerification() {
        SessionUser user = currentState.userOrNull();
        if (user == null || user.isEmailVerified()) {
            return;
        }
        long operation = beginLoading(user);
        gateway.sendVerification(result -> {
            if (!isCurrent(operation)) {
                return;
            }
            publish(AuthState.of(AuthState.Status.UNVERIFIED, user,
                    result.isSuccess()
                            ? AuthState.MessageCode.VERIFICATION_SENT
                            : AuthState.MessageCode.VERIFICATION_SEND_FAILED));
        });
    }

    public void refreshVerification() {
        SessionUser fallback = currentState.userOrNull();
        long operation = beginLoading(fallback);
        reloadForRouting(operation, fallback);
    }

    public void sendPasswordReset(String email) {
        AuthState before = currentState;
        long operation = ++generation;
        gateway.sendPasswordReset(email, result -> {
            if (!isCurrent(operation)) {
                return;
            }
            if (result.isSuccess()) {
                publish(AuthState.of(before.status(), before.userOrNull(),
                        AuthState.MessageCode.RESET_EMAIL_SENT));
            } else {
                publishFailure(result, before.userOrNull());
            }
        });
    }

    public void updatePassword(String newPassword, ResultCallback<Boolean> callback) {
        if (PasswordPolicy.validate(newPassword) != PasswordPolicy.Result.VALID) {
            callback.onResult(AppResult.failure(
                    new AuthGateway.AuthFailure(AuthGateway.FailureCode.WEAK_PASSWORD)));
            return;
        }
        gateway.updatePassword(newPassword, callback);
    }

    public void updateEmail(String newEmail, ResultCallback<Boolean> callback) {
        if (newEmail == null || !newEmail.contains("@") || newEmail.trim().isEmpty()) {
            callback.onResult(AppResult.failure(
                    new AuthGateway.AuthFailure(AuthGateway.FailureCode.INVALID_CREDENTIALS)));
            return;
        }
        gateway.updateEmail(newEmail.trim(), callback);
    }

    public void updateDisplayName(String displayName, ResultCallback<SessionUser> callback) {
        if (displayName == null || displayName.trim().isEmpty()) {
            callback.onResult(AppResult.failure(
                    new AuthGateway.AuthFailure(AuthGateway.FailureCode.INVALID_CREDENTIALS)));
            return;
        }
        gateway.updateDisplayName(displayName.trim(), result -> {
            if (result.isSuccess() && result.valueOrNull() != null) {
                publish(AuthState.of(currentState.status(), result.valueOrNull(),
                        currentState.messageOrNull()));
            }
            callback.onResult(result);
        });
    }

    public void reauthenticate(String email, String password, ResultCallback<Boolean> callback) {
        gateway.reauthenticate(email, password, callback);
    }

    public void signOut() {
        generation++;
        gateway.signOut();
        publish(AuthState.of(AuthState.Status.SIGNED_OUT, null));
    }

    private void bootstrap() {
        if (!gateway.isConfigured()) {
            publish(AuthState.of(AuthState.Status.UNCONFIGURED, null));
            return;
        }
        SessionUser cached = gateway.currentUser();
        if (cached == null) {
            publish(AuthState.of(AuthState.Status.SIGNED_OUT, null));
            return;
        }
        long operation = beginLoading(cached);
        reloadForRouting(operation, cached);
    }

    private void reloadForRouting(long operation, @Nullable SessionUser fallback) {
        gateway.reloadCurrentUser(result -> {
            if (!isCurrent(operation)) {
                return;
            }
            if (result.isSuccess()) {
                routeRefreshed(result.valueOrNull());
                return;
            }
            AuthGateway.FailureCode code = failureCode(result);
            if (code == AuthGateway.FailureCode.NETWORK && fallback != null) {
                if (fallback.isEmailVerified()) {
                    publish(AuthState.of(AuthState.Status.VERIFIED_OFFLINE, fallback,
                            AuthState.MessageCode.OFFLINE));
                } else {
                    publish(AuthState.of(AuthState.Status.UNVERIFIED, fallback,
                            AuthState.MessageCode.NETWORK_REQUIRED));
                }
                return;
            }
            publishFailure(result, fallback);
        });
    }

    private void routeRefreshed(SessionUser user) {
        publish(AuthState.of(
                user.isEmailVerified()
                        ? AuthState.Status.VERIFIED_ONLINE
                        : AuthState.Status.UNVERIFIED,
                user));
    }

    private long beginLoading(@Nullable SessionUser user) {
        long operation = ++generation;
        publish(AuthState.of(AuthState.Status.LOADING, user));
        return operation;
    }

    private <T> void publishFailure(AppResult<T> result, @Nullable SessionUser fallback) {
        AuthGateway.FailureCode code = failureCode(result);
        if (code == AuthGateway.FailureCode.CONFIGURATION) {
            publish(AuthState.of(AuthState.Status.UNCONFIGURED, null));
            return;
        }
        publish(AuthState.of(AuthState.Status.ERROR, fallback, messageFor(code)));
    }

    private static <T> AuthGateway.FailureCode failureCode(AppResult<T> result) {
        Throwable error = result.errorOrNull();
        return error instanceof AuthGateway.AuthFailure
                ? ((AuthGateway.AuthFailure) error).code()
                : AuthGateway.FailureCode.UNKNOWN;
    }

    private static AuthState.MessageCode messageFor(AuthGateway.FailureCode code) {
        switch (code) {
            case INVALID_CREDENTIALS:
                return AuthState.MessageCode.INVALID_CREDENTIALS;
            case EMAIL_IN_USE:
                return AuthState.MessageCode.EMAIL_IN_USE;
            case WEAK_PASSWORD:
                return AuthState.MessageCode.WEAK_PASSWORD;
            case TOO_MANY_REQUESTS:
                return AuthState.MessageCode.TRY_LATER;
            case NETWORK:
                return AuthState.MessageCode.NETWORK_REQUIRED;
            default:
                return AuthState.MessageCode.UNAVAILABLE;
        }
    }

    private boolean isCurrent(long operation) {
        return generation == operation;
    }

    private void publish(AuthState next) {
        currentState = next;
        state.setValue(next);
    }
}
