package com.prottasha.habits.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_substeps",
        foreignKeys = @ForeignKey(entity = HabitEntity.class, parentColumns = "id",
                childColumns = "habitId", onDelete = ForeignKey.CASCADE),
        indices = @Index("habitId"))
public final class HabitSubstepEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public final long habitId;
    @NonNull public final String title;
    public final int sortOrder;

    public HabitSubstepEntity(long id, long habitId, @NonNull String title, int sortOrder) {
        this.id = id;
        this.habitId = habitId;
        this.title = title;
        this.sortOrder = sortOrder;
    }
}
