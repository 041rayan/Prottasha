package com.prottasha.insights.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RehearsalDao {
    @Insert
    long insert(RehearsalSessionEntity session);

    @Query("SELECT * FROM rehearsal_sessions ORDER BY createdAt DESC")
    List<RehearsalSessionEntity> list();

    @Query("SELECT COUNT(*) FROM rehearsal_sessions")
    int count();

    @Query("DELETE FROM rehearsal_sessions WHERE id = :id")
    void deleteById(long id);
}
