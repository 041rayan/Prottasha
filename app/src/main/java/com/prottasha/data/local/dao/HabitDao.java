package com.prottasha.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.prottasha.data.local.entity.HabitEntity;
import java.util.List;

@Dao
public interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HabitEntity habit);

    @Update
    void update(HabitEntity habit);

    @Delete
    void delete(HabitEntity habit);

    @Query("DELETE FROM habits WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    HabitEntity getHabitByIdSync(long id);

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    LiveData<HabitEntity> getHabitById(long id);

    @Query("SELECT * FROM habits ORDER BY id DESC")
    LiveData<List<HabitEntity>> getAllHabits();

    @Query("SELECT * FROM habits ORDER BY id DESC")
    List<HabitEntity> getAllHabitsSync();

    @Query("SELECT * FROM habits WHERE isGoodHabit = :isGood ORDER BY id DESC")
    LiveData<List<HabitEntity>> getHabitsByType(boolean isGood);

    @Query("UPDATE habits SET votesFor = votesFor + 1, currentStreak = currentStreak + 1, consecutiveMisses = 0, lastLoggedTimestamp = :timestamp WHERE id = :id")
    void recordPositiveVote(long id, long timestamp);

    @Query("UPDATE habits SET votesAgainst = votesAgainst + 1, consecutiveMisses = consecutiveMisses + 1, lastLoggedTimestamp = :timestamp WHERE id = :id")
    void recordMiss(long id, long timestamp);
}
