package com.prottasha.focus.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface FocusDao {
    @Query("SELECT * FROM focus_session WHERE id = 1")
    FocusSessionEntity find();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(FocusSessionEntity session);

    @Query("DELETE FROM focus_session")
    void clear();
}
