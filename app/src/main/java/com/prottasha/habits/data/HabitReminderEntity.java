package com.prottasha.habits.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_reminders",
        foreignKeys = @ForeignKey(entity = HabitEntity.class, parentColumns = "id",
                childColumns = "habitId", onDelete = ForeignKey.CASCADE),
        indices = @Index("habitId"))
public final class HabitReminderEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public final long habitId;
    public final int hour;
    public final int minute;
    public final int dayMask;
    public final boolean enabled;

    public HabitReminderEntity(long id, long habitId, int hour, int minute,
            int dayMask, boolean enabled) {
        this.id = id;
        this.habitId = habitId;
        this.hour = hour;
        this.minute = minute;
        this.dayMask = dayMask;
        this.enabled = enabled;
    }
}
