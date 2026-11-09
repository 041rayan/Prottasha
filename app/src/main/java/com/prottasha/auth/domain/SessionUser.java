package com.prottasha.auth.domain;

import androidx.annotation.Nullable;

import java.util.Objects;

public final class SessionUser {

    private final String uid;
    @Nullable
    private final String displayName;
    private final String email;
    private final boolean emailVerified;

    public SessionUser(
            String uid,
            @Nullable String displayName,
            String email,
            boolean emailVerified) {
        this.uid = requireNonBlank(uid, "uid");
        this.displayName = displayName;
        this.email = requireNonBlank(email, "email");
        this.emailVerified = emailVerified;
    }

    public String uid() {
        return uid;
    }

    @Nullable
    public String displayNameOrNull() {
        return displayName;
    }

    public String email() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SessionUser)) {
            return false;
        }
        SessionUser that = (SessionUser) other;
        return emailVerified == that.emailVerified
                && uid.equals(that.uid)
                && Objects.equals(displayName, that.displayName)
                && email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, displayName, email, emailVerified);
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
