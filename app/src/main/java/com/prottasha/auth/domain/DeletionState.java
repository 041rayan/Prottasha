package com.prottasha.auth.domain;

import androidx.annotation.Nullable;

public final class DeletionState {

    public enum Stage {
        IDLE,
        REAUTHENTICATING,
        QUARANTINING_LOCAL,
        DELETING_CLOUD,
        DELETING_AUTH,
        RESTORING_LOCAL,
        CLEANING_LOCAL,
        RETRY_LOCAL_RESTORE,
        RETRY_LOCAL_CLEANUP,
        COMPLETE,
        FAILED
    }

    public enum Failure {
        INVALID_CREDENTIALS,
        REAUTHENTICATION,
        CLOUD,
        REQUIRES_RECENT_LOGIN,
        AUTH,
        LOCAL_QUARANTINE,
        LOCAL_RESTORE,
        LOCAL_CLEANUP,
        INTERRUPTED,
        JOURNAL
    }

    private final Stage stage;
    @Nullable
    private final Failure failure;
    @Nullable
    private final String uidHash;

    private DeletionState(Stage stage, @Nullable Failure failure, @Nullable String uidHash) {
        this.stage = stage;
        this.failure = failure;
        this.uidHash = uidHash;
    }

    public static DeletionState idle() {
        return new DeletionState(Stage.IDLE, null, null);
    }

    public static DeletionState at(Stage stage, String uidHash) {
        return new DeletionState(stage, null, uidHash);
    }

    public static DeletionState failed(Failure failure, @Nullable String uidHash) {
        return new DeletionState(Stage.FAILED, failure, uidHash);
    }

    public Stage stage() {
        return stage;
    }

    @Nullable
    public Failure failureOrNull() {
        return failure;
    }

    @Nullable
    public String uidHashOrNull() {
        return uidHash;
    }
}
