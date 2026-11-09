package com.prottasha.habits.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY sortOrder, id")
    List<HabitEntity> listActive();

    @Query("SELECT * FROM habits WHERE id = :habitId")
    HabitEntity find(long habitId);

    @Insert
    long insert(HabitEntity habit);

    @Update
    void update(HabitEntity habit);

    @Query("UPDATE habits SET archived = :archived WHERE id = :habitId")
    void setArchived(long habitId, boolean archived);

    @Query("UPDATE habits SET sortOrder = :sortOrder WHERE id = :habitId")
    void setSortOrder(long habitId, int sortOrder);

    @Query("DELETE FROM habits WHERE id = :habitId")
    void delete(long habitId);

    @Query("SELECT * FROM habit_events WHERE nonce = :nonce LIMIT 1")
    HabitEventEntity findEventByNonce(String nonce);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertEvent(HabitEventEntity event);

    @Query("SELECT COUNT(*) FROM habit_events WHERE nonce = :nonce")
    int countEventsByNonce(String nonce);

    @Query("SELECT * FROM habit_day_aggregates WHERE habitId = :habitId AND epochDay = :epochDay")
    HabitDayAggregateEntity findAggregate(long habitId, long epochDay);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAggregate(HabitDayAggregateEntity aggregate);

    @Query("SELECT * FROM habit_day_aggregates WHERE habitId = :habitId ORDER BY epochDay DESC LIMIT :limit")
    List<HabitDayAggregateEntity> listAggregates(long habitId, int limit);

    @Query("SELECT * FROM habit_day_notes WHERE habitId = :habitId AND epochDay = :epochDay")
    HabitDayNoteEntity findNote(long habitId, long epochDay);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertNote(HabitDayNoteEntity note);

    @Query("SELECT * FROM habit_reminders WHERE enabled = 1")
    List<HabitReminderEntity> listEnabledReminders();

    @Query("SELECT * FROM habit_reminders WHERE id = :reminderId")
    HabitReminderEntity findReminder(long reminderId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long upsertReminder(HabitReminderEntity reminder);
}
