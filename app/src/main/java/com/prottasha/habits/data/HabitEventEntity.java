package com.prottasha.habits.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_events",
        foreignKeys = @ForeignKey(entity = HabitEntity.class, parentColumns = "id",
                childColumns = "habitId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("habitId"), @Index(value = "nonce", unique = true)})
public final class HabitEventEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public final long habitId;
    public final long epochDay;
    @NonNull public final String kind;
    public final int amount;
    @NonNull public final String nonce;
    public final long revertedEventId;
    public final long occurredAt;
    @NonNull public final String zoneId;
    public final int cutoffMinutes;

    public HabitEventEntity(long id, long habitId, long epochDay, @NonNull String kind,
            int amount, @NonNull String nonce, long revertedEventId, long occurredAt,
            @NonNull String zoneId, int cutoffMinutes) {
        this.id = id;
        this.habitId = habitId;
        this.epochDay = epochDay;
        this.kind = kind;
        this.amount = amount;
        this.nonce = nonce;
        this.revertedEventId = revertedEventId;
        this.occurredAt = occurredAt;
        this.zoneId = zoneId;
        this.cutoffMinutes = cutoffMinutes;
    }
}
