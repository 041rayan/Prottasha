package com.prottasha.triggers.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TriggerDao {
    @Insert
    long insert(TriggerPlanEntity plan);

    @Update
    void update(TriggerPlanEntity plan);

    @Query("SELECT * FROM trigger_plans ORDER BY createdAt DESC")
    List<TriggerPlanEntity> list();

    @Query("DELETE FROM trigger_plans WHERE id = :id")
    void deleteById(long id);
}
