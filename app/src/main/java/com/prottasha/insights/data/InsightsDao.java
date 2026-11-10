package com.prottasha.insights.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InsightsDao {
    @Insert
    long insert(JournalEntryEntity entry);

    @Update
    void update(JournalEntryEntity entry);

    @Query("SELECT * FROM reflections ORDER BY createdAt DESC LIMIT :limit")
    List<JournalEntryEntity> recent(int limit);

    @Query("SELECT * FROM reflections ORDER BY createdAt DESC")
    List<JournalEntryEntity> listAll();

    @Query("SELECT * FROM reflections WHERE id = :id")
    JournalEntryEntity findById(long id);

    @Query("SELECT * FROM reflections WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY createdAt DESC")
    List<JournalEntryEntity> listRange(long startEpochDay, long endEpochDay);

    @Query("DELETE FROM reflections WHERE id = :id")
    void deleteById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertImage(JournalImageEntity image);

    @Query("SELECT * FROM reflection_images WHERE entryId = :entryId")
    List<JournalImageEntity> findImagesForEntry(long entryId);

    @Query("DELETE FROM reflection_images WHERE id = :id")
    void deleteImageById(long id);

    @Query("DELETE FROM reflection_images WHERE entryId = :entryId")
    void deleteImagesForEntry(long entryId);
}
