package com.prottasha.data.local.user.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "import_journal")
public final class ImportJournalEntity {

    @PrimaryKey
    @NonNull
    public final String sourceHash;
    @NonNull
    public final String state;
    @NonNull
    public final String expectedCountsJson;
    @NonNull
    public final String copiedCountsJson;
    public final long updatedAt;

    public ImportJournalEntity(
            @NonNull String sourceHash,
            @NonNull String state,
            @NonNull String expectedCountsJson,
            @NonNull String copiedCountsJson,
            long updatedAt) {
        this.sourceHash = sourceHash;
        this.state = state;
        this.expectedCountsJson = expectedCountsJson;
        this.copiedCountsJson = copiedCountsJson;
        this.updatedAt = updatedAt;
    }
}
