package com.prottasha.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.prottasha.data.local.entity.JournalEntryEntity;
import java.util.List;

@Dao
public interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(JournalEntryEntity entry);

    @Update
    void update(JournalEntryEntity entry);

    @Query("SELECT * FROM journal_entries ORDER BY dateEpochDay DESC")
    LiveData<List<JournalEntryEntity>> getAllEntries();

    @Query("SELECT * FROM journal_entries WHERE dateEpochDay = :epochDay LIMIT 1")
    JournalEntryEntity getEntryByDateSync(long epochDay);

    @Query("SELECT winsText FROM journal_entries WHERE winsText IS NOT NULL AND winsText != '' ORDER BY dateEpochDay DESC LIMIT 10")
    LiveData<List<String>> getRecentSuccessMemories();
}
