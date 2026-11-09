package com.prottasha.insights.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InsightsDao {
    @Insert long insert(JournalEntryEntity entry);
    @Query("SELECT * FROM reflections ORDER BY createdAt DESC LIMIT :limit")
    List<JournalEntryEntity> recent(int limit);
}
