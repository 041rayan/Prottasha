package com.prottasha.intervention.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InterventionDao {
    @Insert long insert(InterventionEventEntity event);

    @Query("SELECT * FROM intervention_events ORDER BY timestamp DESC LIMIT :limit")
    List<InterventionEventEntity> recent(int limit);
}
