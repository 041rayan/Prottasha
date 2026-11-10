package com.prottasha.habits.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public final class HabitEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public final String title;
    @NonNull public final String type;
    public final int target;
    @NonNull public final String scheduleFrequency;
    public final long startEpochDay;
    public final int scheduleInterval;
    public final int scheduleDayOfMonth;
    public final boolean archived;
    public final int sortOrder;
    public final long createdAt;
    @NonNull public final String cue;
    @NonNull public final String twoMinuteAction;
    @NonNull public final String reward;
    @NonNull public final String ifThenPlan;
    @NonNull public final String reboundPlan;
    @NonNull public final String unit;
    public final int incrementVal;
    public final int maximumVal;
    public final int focusMinutes;
    @NonNull public final String accountability;

    public HabitEntity(
            long id,
            @NonNull String title,
            @NonNull String type,
            int target,
            @NonNull String scheduleFrequency,
            long startEpochDay,
            int scheduleInterval,
            int scheduleDayOfMonth,
            boolean archived,
            int sortOrder,
            long createdAt,
            @NonNull String cue,
            @NonNull String twoMinuteAction,
            @NonNull String reward,
            @NonNull String ifThenPlan,
            @NonNull String reboundPlan,
            @NonNull String unit,
            int incrementVal,
            int maximumVal,
            int focusMinutes,
            @NonNull String accountability) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.target = target;
        this.scheduleFrequency = scheduleFrequency;
        this.startEpochDay = startEpochDay;
        this.scheduleInterval = scheduleInterval;
        this.scheduleDayOfMonth = scheduleDayOfMonth;
        this.archived = archived;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.cue = cue;
        this.twoMinuteAction = twoMinuteAction;
        this.reward = reward;
        this.ifThenPlan = ifThenPlan;
        this.reboundPlan = reboundPlan;
        this.unit = unit;
        this.incrementVal = incrementVal;
        this.maximumVal = maximumVal;
        this.focusMinutes = focusMinutes;
        this.accountability = accountability;
    }

    @Ignore
    public HabitEntity(long id, @NonNull String title, @NonNull String type, int target,
            @NonNull String scheduleFrequency, long startEpochDay, int scheduleInterval,
            int scheduleDayOfMonth, boolean archived, int sortOrder, long createdAt) {
        this(id, title, type, target, scheduleFrequency, startEpochDay, scheduleInterval,
                scheduleDayOfMonth, archived, sortOrder, createdAt, "", "", "", "", "", "", 1, 0, 0, "");
    }
}
