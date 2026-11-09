package com.prottasha.guard.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "guard_rules", indices = @Index(value = "target", unique = true))
public final class GuardRuleEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public final String target;
    @NonNull public final String label;
    @NonNull public final String mode;
    public final int budgetMinutes;
    public final int startMinute;
    public final int endMinute;
    public final boolean enabled;

    public GuardRuleEntity(long id, @NonNull String target, @NonNull String label,
            @NonNull String mode, int budgetMinutes, int startMinute,
            int endMinute, boolean enabled) {
        this.id = id;
        this.target = target;
        this.label = label;
        this.mode = mode;
        this.budgetMinutes = budgetMinutes;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.enabled = enabled;
    }
}
