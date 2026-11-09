package com.prottasha.auth.domain;

import com.prottasha.auth.data.AuthGateway;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

public final class ReauthenticationCoordinator {

    private final AuthGateway authGateway;

    public ReauthenticationCoordinator(AuthGateway authGateway) {
        this.authGateway = authGateway;
    }

    public void reauthenticate(
            SessionUser user,
            String password,
            ResultCallback<Boolean> callback) {
        if (user == null || password == null || password.isEmpty()) {
            callback.onResult(AppResult.failure(new AuthGateway.AuthFailure(
                    AuthGateway.FailureCode.INVALID_CREDENTIALS)));
            return;
        }
        authGateway.reauthenticate(user.email(), password, callback);
    }
}
