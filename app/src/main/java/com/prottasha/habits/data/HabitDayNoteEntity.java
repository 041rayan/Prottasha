package com.prottasha.habits.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "habit_day_notes", primaryKeys = {"habitId", "epochDay"},
        foreignKeys = @ForeignKey(entity = HabitEntity.class, parentColumns = "id",
                childColumns = "habitId", onDelete = ForeignKey.CASCADE))
public final class HabitDayNoteEntity {
    public final long habitId;
    public final long epochDay;
    @NonNull public final String note;
    public final long updatedAt;

    public HabitDayNoteEntity(long habitId, long epochDay, @NonNull String note, long updatedAt) {
        this.habitId = habitId;
        this.epochDay = epochDay;
        this.note = note;
        this.updatedAt = updatedAt;
    }
}
