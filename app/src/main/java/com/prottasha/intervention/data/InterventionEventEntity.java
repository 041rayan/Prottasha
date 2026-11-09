package com.prottasha.intervention.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "intervention_events")
public final class InterventionEventEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public final String target;
    @NonNull public final String type;
    public final long timestamp;
    @NonNull public final String intention;

    public InterventionEventEntity(long id, @NonNull String target, @NonNull String type,
            long timestamp, @NonNull String intention) {
        this.id = id;
        this.target = target;
        this.type = type;
        this.timestamp = timestamp;
        this.intention = intention;
    }
}
