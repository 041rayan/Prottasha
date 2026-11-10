package com.prottasha.guard.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "guard_rules", indices = @Index(value = "target", unique = true))
public final class GuardRuleEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public final String target;

    @NonNull
    public final String label;

    @NonNull
    public final String mode;

    public final int budgetMinutes;
    public final int startMinute;
    public final int endMinute;
    public final boolean enabled;

    public final int cooldownMinutes;
    public final int delaySeconds;
    public final int graceMinutes;
    public final boolean escalation;

    @NonNull
    public final String replacementAction;

    public final boolean editProtection;
    public final boolean isDomain;

    public GuardRuleEntity(
            long id,
            @NonNull String target,
            @NonNull String label,
            @NonNull String mode,
            int budgetMinutes,
            int startMinute,
            int endMinute,
            boolean enabled,
            int cooldownMinutes,
            int delaySeconds,
            int graceMinutes,
            boolean escalation,
            @NonNull String replacementAction,
            boolean editProtection,
            boolean isDomain) {
        this.id = id;
        this.target = target;
        this.label = label;
        this.mode = mode;
        this.budgetMinutes = budgetMinutes;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.enabled = enabled;
        this.cooldownMinutes = cooldownMinutes > 0 ? cooldownMinutes : 15;
        this.delaySeconds = delaySeconds > 0 ? delaySeconds : 5;
        this.graceMinutes = graceMinutes >= 0 ? graceMinutes : 5;
        this.escalation = escalation;
        this.replacementAction = replacementAction != null ? replacementAction : "";
        this.editProtection = editProtection;
        this.isDomain = isDomain;
    }

    @Ignore
    public GuardRuleEntity(
            long id,
            @NonNull String target,
            @NonNull String label,
            @NonNull String mode,
            int budgetMinutes,
            int startMinute,
            int endMinute,
            boolean enabled) {
        this(id, target, label, mode, budgetMinutes, startMinute, endMinute, enabled,
                15, 5, 5, false, "", false, false);
    }
}
