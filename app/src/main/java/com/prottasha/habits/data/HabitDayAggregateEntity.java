package com.prottasha.habits.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "habit_day_aggregates", primaryKeys = {"habitId", "epochDay"},
        foreignKeys = @ForeignKey(entity = HabitEntity.class, parentColumns = "id",
                childColumns = "habitId", onDelete = ForeignKey.CASCADE),
        indices = @Index("habitId"))
public final class HabitDayAggregateEntity {
    public final long habitId;
    public final long epochDay;
    public final int value;
    public final boolean completed;
    public final boolean lapsed;
    public final long updatedAt;

    public HabitDayAggregateEntity(long habitId, long epochDay, int value,
            boolean completed, boolean lapsed, long updatedAt) {
        this.habitId = habitId;
        this.epochDay = epochDay;
        this.value = value;
        this.completed = completed;
        this.lapsed = lapsed;
        this.updatedAt = updatedAt;
    }
}
