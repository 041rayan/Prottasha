package com.prottasha.habits.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
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

    public HabitEntity(long id, @NonNull String title, @NonNull String type, int target,
            @NonNull String scheduleFrequency, long startEpochDay, int scheduleInterval,
            int scheduleDayOfMonth, boolean archived, int sortOrder, long createdAt) {
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
    }
}
