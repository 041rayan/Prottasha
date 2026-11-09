package com.prottasha.guard.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "guard_schedules", primaryKeys = {"ruleId", "dayOfWeek"},
        foreignKeys = @ForeignKey(entity = GuardRuleEntity.class, parentColumns = "id",
                childColumns = "ruleId", onDelete = ForeignKey.CASCADE))
public final class GuardScheduleEntity {
    public final long ruleId;
    public final int dayOfWeek;
    public final boolean enabled;

    public GuardScheduleEntity(long ruleId, int dayOfWeek, boolean enabled) {
        this.ruleId = ruleId;
        this.dayOfWeek = dayOfWeek;
        this.enabled = enabled;
    }
}
