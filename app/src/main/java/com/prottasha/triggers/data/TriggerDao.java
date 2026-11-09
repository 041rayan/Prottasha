package com.prottasha.triggers.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TriggerDao {
    @Insert long insert(TriggerPlanEntity plan);
    @Query("SELECT * FROM trigger_plans ORDER BY createdAt DESC")
    List<TriggerPlanEntity> list();
}
