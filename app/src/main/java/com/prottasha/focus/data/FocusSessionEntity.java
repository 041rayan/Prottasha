package com.prottasha.focus.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_session")
public final class FocusSessionEntity {
    @PrimaryKey public final int id;
    public final long durationMillis;
    public final long startedAt;
    public final long accumulatedMillis;
    @NonNull public final String state;

    public FocusSessionEntity(int id, long durationMillis, long startedAt,
            long accumulatedMillis, @NonNull String state) {
        this.id = id;
        this.durationMillis = durationMillis;
        this.startedAt = startedAt;
        this.accumulatedMillis = accumulatedMillis;
        this.state = state;
    }
}
