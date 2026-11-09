package com.prottasha.auth.domain;

import androidx.annotation.Nullable;

import java.util.Objects;

public final class AuthState {

    public enum Status {
        UNCONFIGURED,
        SIGNED_OUT,
        LOADING,
        UNVERIFIED,
        VERIFIED_ONLINE,
        VERIFIED_OFFLINE,
        DELETING,
        ERROR
    }

    public enum MessageCode {
        OFFLINE,
        NETWORK_REQUIRED,
        VERIFICATION_SENT,
        VERIFICATION_SEND_FAILED,
        INVALID_CREDENTIALS,
        EMAIL_IN_USE,
        WEAK_PASSWORD,
        TRY_LATER,
        RESET_EMAIL_SENT,
        UNAVAILABLE
    }

    private final Status status;
    @Nullable
    private final SessionUser user;
    @Nullable
    private final MessageCode message;

    private AuthState(Status status, @Nullable SessionUser user, @Nullable MessageCode message) {
        this.status = Objects.requireNonNull(status, "status");
        this.user = user;
        this.message = message;
    }

    public static AuthState of(Status status, @Nullable SessionUser user) {
        return new AuthState(status, user, null);
    }

    public static AuthState of(
            Status status,
            @Nullable SessionUser user,
            @Nullable MessageCode message) {
        return new AuthState(status, user, message);
    }

    public Status status() {
        return status;
    }

    @Nullable
    public SessionUser userOrNull() {
        return user;
    }

    @Nullable
    public MessageCode messageOrNull() {
        return message;
    }
}
