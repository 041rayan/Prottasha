package com.prottasha.triggers.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trigger_plans")
public final class TriggerPlanEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public final String cue;
    @NonNull public final String plan;
    public final long createdAt;

    public TriggerPlanEntity(long id, @NonNull String cue, @NonNull String plan, long createdAt) {
        this.id = id;
        this.cue = cue;
        this.plan = plan;
        this.createdAt = createdAt;
    }
}
