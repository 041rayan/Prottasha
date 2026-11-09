package com.prottasha.habits.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_day_images",
        foreignKeys = @ForeignKey(entity = HabitEntity.class, parentColumns = "id",
                childColumns = "habitId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("habitId"), @Index(value = "storageId", unique = true)})
public final class HabitDayImageEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public final long habitId;
    public final long epochDay;
    @NonNull public final String storageId;

    public HabitDayImageEntity(long id, long habitId, long epochDay, @NonNull String storageId) {
        this.id = id;
        this.habitId = habitId;
        this.epochDay = epochDay;
        this.storageId = storageId;
    }
}
