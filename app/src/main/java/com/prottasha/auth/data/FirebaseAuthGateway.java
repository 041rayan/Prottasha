package com.prottasha.auth.data;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

public final class FirebaseAuthGateway implements AuthGateway {

    @Nullable
    private final FirebaseAuth auth;

    public FirebaseAuthGateway() {
        FirebaseAuth configured;
        try {
            configured = FirebaseAuth.getInstance();
        } catch (RuntimeException unavailable) {
            configured = null;
        }
        auth = configured;
    }

    @Override
    public boolean isConfigured() {
        return auth != null;
    }

    @Nullable
    @Override
    public SessionUser currentUser() {
        return auth == null ? null : mapUserOrNull(auth.getCurrentUser());
    }

    @Override
    public void createAccount(
            String displayName,
            String email,
            String password,
            ResultCallback<SessionUser> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(created -> {
            if (!created.isSuccessful()) {
                callback.onResult(failure(created.getException()));
                return;
            }
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                callback.onResult(unknownFailure());
                return;
            }
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(updated -> {
                if (!updated.isSuccessful()) {
                    callback.onResult(failure(updated.getException()));
                    return;
                }
                callback.onResult(userResult(auth.getCurrentUser()));
            });
        });
    }

    @Override
    public void signIn(String email, String password, ResultCallback<SessionUser> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onResult(userResult(auth.getCurrentUser()));
            } else {
                callback.onResult(failure(task.getException()));
            }
        });
    }

    @Override
    public void reloadCurrentUser(ResultCallback<SessionUser> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(unknownFailure());
            return;
        }
        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onResult(userResult(auth.getCurrentUser()));
            } else {
                callback.onResult(failure(task.getException()));
            }
        });
    }

    @Override
    public void sendVerification(ResultCallback<Boolean> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(unknownFailure());
            return;
        }
        user.sendEmailVerification().addOnCompleteListener(task -> callback.onResult(
                task.isSuccessful()
                        ? AppResult.success(true)
                        : failure(task.getException())));
    }

    @Override
    public void sendPasswordReset(String email, ResultCallback<Boolean> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> callback.onResult(
                task.isSuccessful()
                        ? AppResult.success(true)
                        : failure(task.getException())));
    }

    @Override
    public void reauthenticate(
            String email,
            String password,
            ResultCallback<Boolean> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(unknownFailure());
            return;
        }
        user.reauthenticate(EmailAuthProvider.getCredential(email, password))
                .addOnCompleteListener(task -> callback.onResult(
                        task.isSuccessful()
                                ? AppResult.success(true)
                                : failure(task.getException())));
    }

    @Override
    public void updatePassword(String newPassword, ResultCallback<Boolean> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(unknownFailure());
            return;
        }
        user.updatePassword(newPassword).addOnCompleteListener(task -> callback.onResult(
                task.isSuccessful()
                        ? AppResult.success(true)
                        : failure(task.getException())));
    }

    @Override
    public void updateEmail(String newEmail, ResultCallback<Boolean> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(unknownFailure());
            return;
        }
        user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> callback.onResult(
                task.isSuccessful()
                        ? AppResult.success(true)
                        : failure(task.getException())));
    }

    @Override
    public void updateDisplayName(String displayName, ResultCallback<SessionUser> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(unknownFailure());
            return;
        }
        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();
        user.updateProfile(profile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onResult(userResult(auth.getCurrentUser()));
            } else {
                callback.onResult(failure(task.getException()));
            }
        });
    }

    @Override
    public void deleteCurrentUser(ResultCallback<Boolean> callback) {
        if (auth == null) {
            callback.onResult(configurationFailure());
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(unknownFailure());
            return;
        }
        user.delete().addOnCompleteListener(task -> callback.onResult(
                task.isSuccessful()
                        ? AppResult.success(true)
                        : failure(task.getException())));
    }

    @Override
    public void signOut() {
        if (auth != null) {
            auth.signOut();
        }
    }

    static AuthFailure mapFailure(@Nullable Throwable error) {
        if (error instanceof FirebaseNetworkException) {
            return new AuthFailure(FailureCode.NETWORK);
        }
        if (error instanceof FirebaseTooManyRequestsException) {
            return new AuthFailure(FailureCode.TOO_MANY_REQUESTS);
        }
        if (error instanceof FirebaseAuthWeakPasswordException) {
            return new AuthFailure(FailureCode.WEAK_PASSWORD);
        }
        if (error instanceof FirebaseAuthRecentLoginRequiredException) {
            return new AuthFailure(FailureCode.REQUIRES_RECENT_LOGIN);
        }
        if (error instanceof FirebaseAuthUserCollisionException) {
            return new AuthFailure(FailureCode.EMAIL_IN_USE);
        }
        if (error instanceof FirebaseAuthInvalidCredentialsException) {
            return new AuthFailure(FailureCode.INVALID_CREDENTIALS);
        }
        return new AuthFailure(FailureCode.UNKNOWN);
    }

    private static SessionUser mapUserOrNull(@Nullable FirebaseUser user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return null;
        }
        return new SessionUser(
                user.getUid(), user.getDisplayName(), user.getEmail(), user.isEmailVerified());
    }

    private static AppResult<SessionUser> userResult(@Nullable FirebaseUser user) {
        SessionUser mapped = mapUserOrNull(user);
        return mapped == null ? unknownFailure() : AppResult.success(mapped);
    }

    private static <T> AppResult<T> failure(@Nullable Throwable error) {
        return AppResult.failure(mapFailure(error));
    }

    private static <T> AppResult<T> configurationFailure() {
        return AppResult.failure(new AuthFailure(FailureCode.CONFIGURATION));
    }

    private static <T> AppResult<T> unknownFailure() {
        return AppResult.failure(new AuthFailure(FailureCode.UNKNOWN));
    }
}
