package com.prottasha.habits.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "habit_vacations", primaryKeys = {"habitId", "startEpochDay"},
        foreignKeys = @ForeignKey(entity = HabitEntity.class, parentColumns = "id",
                childColumns = "habitId", onDelete = ForeignKey.CASCADE))
public final class HabitVacationEntity {
    public final long habitId;
    public final long startEpochDay;
    public final long endEpochDay;

    public HabitVacationEntity(long habitId, long startEpochDay, long endEpochDay) {
        this.habitId = habitId;
        this.startEpochDay = startEpochDay;
        this.endEpochDay = endEpochDay;
    }
}
