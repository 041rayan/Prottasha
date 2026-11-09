package com.prottasha.data.local.user.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prottasha.data.local.user.entity.ImportJournalEntity;

import java.util.List;

@Dao
public interface ImportJournalDao {

    @Nullable
    @Query("SELECT * FROM import_journal WHERE sourceHash = :sourceHash")
    ImportJournalEntity find(String sourceHash);

    @Query("SELECT * FROM import_journal ORDER BY updatedAt DESC")
    List<ImportJournalEntity> list();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ImportJournalEntity journal);

    @Query("DELETE FROM import_journal WHERE sourceHash = :sourceHash")
    void delete(String sourceHash);
}
