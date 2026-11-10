package com.prottasha.insights.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rehearsal_sessions")
public final class RehearsalSessionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public final String scenario;

    public final int durationSeconds;
    public final int feltIntensity;
    public final long createdAt;

    public RehearsalSessionEntity(long id, @NonNull String scenario,
            int durationSeconds, int feltIntensity, long createdAt) {
        this.id = id;
        this.scenario = scenario;
        this.durationSeconds = durationSeconds;
        this.feltIntensity = feltIntensity;
        this.createdAt = createdAt;
    }
}
