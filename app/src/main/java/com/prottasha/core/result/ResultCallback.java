package com.prottasha.core.result;

@FunctionalInterface
public interface ResultCallback<T> {
    void onResult(AppResult<T> result);
}
