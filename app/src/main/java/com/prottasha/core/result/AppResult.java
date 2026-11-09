package com.prottasha.core.result;

import androidx.annotation.Nullable;

import java.util.Objects;

public final class AppResult<T> {

    @Nullable
    private final T value;
    @Nullable
    private final Throwable error;

    private AppResult(@Nullable T value, @Nullable Throwable error) {
        this.value = value;
        this.error = error;
    }

    public static <T> AppResult<T> success(T value) {
        return new AppResult<>(Objects.requireNonNull(value, "value"), null);
    }

    public static <T> AppResult<T> failure(Throwable error) {
        return new AppResult<>(null, Objects.requireNonNull(error, "error"));
    }

    public boolean isSuccess() {
        return error == null;
    }

    @Nullable
    public T valueOrNull() {
        return value;
    }

    @Nullable
    public Throwable errorOrNull() {
        return error;
    }
}
