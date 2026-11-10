package com.prottasha.intervention.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "intervention_events")
public final class InterventionEventEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public final long ruleId;

    @NonNull
    public final String target;

    @NonNull
    public final String type;

    public final long timestamp;
    public final long resolvedAt;
    public final int delaySeconds;

    @NonNull
    public final String source;

    @NonNull
    public final String outcome;

    @NonNull
    public final String intention;

    @NonNull
    public final String followThrough;

    public InterventionEventEntity(
            long id,
            long ruleId,
            @NonNull String target,
            @NonNull String type,
            long timestamp,
            long resolvedAt,
            int delaySeconds,
            @NonNull String source,
            @NonNull String outcome,
            @NonNull String intention,
            @NonNull String followThrough) {
        this.id = id;
        this.ruleId = ruleId;
        this.target = target;
        this.type = type;
        this.timestamp = timestamp;
        this.resolvedAt = resolvedAt;
        this.delaySeconds = delaySeconds;
        this.source = source != null ? source : "ACCESSIBILITY";
        this.outcome = outcome != null ? outcome : "";
        this.intention = intention != null ? intention : "";
        this.followThrough = followThrough != null ? followThrough : "PENDING";
    }

    @Ignore
    public InterventionEventEntity(
            long id,
            @NonNull String target,
            @NonNull String type,
            long timestamp,
            @NonNull String intention) {
        this(id, 0L, target, type, timestamp, 0L, 0, "ACCESSIBILITY", "", intention, "PENDING");
    }
}
