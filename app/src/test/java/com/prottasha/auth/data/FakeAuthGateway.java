package com.prottasha.auth.data;

import androidx.annotation.Nullable;

import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public final class FakeAuthGateway implements AuthGateway {

    public boolean configured = true;
    @Nullable public SessionUser currentUser;
    @Nullable public AppResult<SessionUser> createResult;
    @Nullable public AppResult<SessionUser> signInResult;
    @Nullable public AppResult<SessionUser> reloadResult;
    public AppResult<Boolean> verificationResult = AppResult.success(true);
    public AppResult<Boolean> resetResult = AppResult.success(true);
    public AppResult<Boolean> reauthenticationResult = AppResult.success(true);
    public AppResult<Boolean> updatePasswordResult = AppResult.success(true);
    public AppResult<Boolean> updateEmailResult = AppResult.success(true);
    @Nullable public AppResult<SessionUser> updateDisplayNameResult;
    public AppResult<Boolean> deleteResult = AppResult.success(true);
    @Nullable public List<String> events;
    public boolean holdSignIn;
    public int signOutCalls;
    public int verificationCalls;
    public int resetCalls;
    public int deleteCalls;
    public int updatePasswordCalls;
    public int updateEmailCalls;
    public int updateDisplayNameCalls;

    private final Queue<Runnable> pending = new ArrayDeque<>();

    @Override
    public boolean isConfigured() {
        return configured;
    }

    @Nullable
    @Override
    public SessionUser currentUser() {
        return currentUser;
    }

    @Override
    public void createAccount(
            String displayName,
            String email,
            String password,
            ResultCallback<SessionUser> callback) {
        callback.onResult(require(createResult));
    }

    @Override
    public void signIn(String email, String password, ResultCallback<SessionUser> callback) {
        Runnable delivery = () -> callback.onResult(require(signInResult));
        if (holdSignIn) {
            pending.add(delivery);
        } else {
            delivery.run();
        }
    }

    @Override
    public void reloadCurrentUser(ResultCallback<SessionUser> callback) {
        callback.onResult(require(reloadResult));
    }

    @Override
    public void sendVerification(ResultCallback<Boolean> callback) {
        verificationCalls++;
        callback.onResult(verificationResult);
    }

    @Override
    public void sendPasswordReset(String email, ResultCallback<Boolean> callback) {
        resetCalls++;
        callback.onResult(resetResult);
    }

    @Override
    public void reauthenticate(
            String email,
            String password,
            ResultCallback<Boolean> callback) {
        if (events != null) {
            events.add("reauth");
        }
        callback.onResult(reauthenticationResult);
    }

    @Override
    public void updatePassword(String newPassword, ResultCallback<Boolean> callback) {
        updatePasswordCalls++;
        callback.onResult(updatePasswordResult);
    }

    @Override
    public void updateEmail(String newEmail, ResultCallback<Boolean> callback) {
        updateEmailCalls++;
        callback.onResult(updateEmailResult);
    }

    @Override
    public void updateDisplayName(String displayName, ResultCallback<SessionUser> callback) {
        updateDisplayNameCalls++;
        callback.onResult(require(updateDisplayNameResult));
    }

    @Override
    public void deleteCurrentUser(ResultCallback<Boolean> callback) {
        if (events != null) {
            events.add("auth-delete");
        }
        deleteCalls++;
        callback.onResult(deleteResult);
    }

    @Override
    public void signOut() {
        signOutCalls++;
        currentUser = null;
    }

    public void deliverPending() {
        Runnable callback = pending.poll();
        if (callback == null) {
            throw new AssertionError("No callback is pending");
        }
        callback.run();
    }

    private static <T> AppResult<T> require(@Nullable AppResult<T> result) {
        if (result == null) {
            throw new AssertionError("Fake result was not configured");
        }
        return result;
    }
}
