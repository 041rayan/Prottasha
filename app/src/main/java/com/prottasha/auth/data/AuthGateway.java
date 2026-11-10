package com.prottasha.auth.data;

import androidx.annotation.Nullable;

import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.ResultCallback;

public interface AuthGateway {

    enum FailureCode {
        CONFIGURATION,
        NETWORK,
        INVALID_CREDENTIALS,
        EMAIL_IN_USE,
        WEAK_PASSWORD,
        TOO_MANY_REQUESTS,
        REQUIRES_RECENT_LOGIN,
        UNKNOWN
    }

    final class AuthFailure extends Exception {
        private final FailureCode code;

        public AuthFailure(FailureCode code) {
            super(code.name());
            this.code = code;
        }

        public FailureCode code() {
            return code;
        }
    }

    boolean isConfigured();

    @Nullable
    SessionUser currentUser();

    void createAccount(
            String displayName,
            String email,
            String password,
            ResultCallback<SessionUser> callback);

    void signIn(String email, String password, ResultCallback<SessionUser> callback);

    void reloadCurrentUser(ResultCallback<SessionUser> callback);

    void sendVerification(ResultCallback<Boolean> callback);

    void sendPasswordReset(String email, ResultCallback<Boolean> callback);

    void reauthenticate(String email, String password, ResultCallback<Boolean> callback);

    void updatePassword(String newPassword, ResultCallback<Boolean> callback);

    void updateEmail(String newEmail, ResultCallback<Boolean> callback);

    void updateDisplayName(String displayName, ResultCallback<SessionUser> callback);

    void deleteCurrentUser(ResultCallback<Boolean> callback);

    void signOut();
}
