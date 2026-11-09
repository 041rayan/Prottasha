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
}
