package com.prottasha.insights.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reflections")
public final class JournalEntryEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public final long epochDay;
    @NonNull public final String fact;
    @NonNull public final String interpretation;
    @NonNull public final String lesson;
    public final long createdAt;

    public JournalEntryEntity(long id, long epochDay, @NonNull String fact,
            @NonNull String interpretation, @NonNull String lesson, long createdAt) {
        this.id = id;
        this.epochDay = epochDay;
        this.fact = fact;
        this.interpretation = interpretation;
        this.lesson = lesson;
        this.createdAt = createdAt;
    }
}
