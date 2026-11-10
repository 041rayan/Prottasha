package com.prottasha.usage.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UsageDailyEntity usage);

    @Query("SELECT * FROM usage_daily WHERE epochDay = :epochDay ORDER BY totalMillis DESC")
    List<UsageDailyEntity> listDay(long epochDay);

    @Query("SELECT * FROM usage_daily WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY totalMillis DESC")
    List<UsageDailyEntity> listRange(long startEpochDay, long endEpochDay);

    @Query("SELECT * FROM usage_daily WHERE target = :target AND epochDay = :epochDay")
    UsageDailyEntity find(String target, long epochDay);

    @Query("SELECT * FROM usage_daily WHERE target = :target AND epochDay BETWEEN :startEpochDay AND :endEpochDay")
    List<UsageDailyEntity> listTargetRange(String target, long startEpochDay, long endEpochDay);
}
