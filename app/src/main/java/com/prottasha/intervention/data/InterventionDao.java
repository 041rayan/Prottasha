package com.prottasha.intervention.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InterventionDao {

    @Insert
    long insert(InterventionEventEntity event);

    @Query("SELECT * FROM intervention_events ORDER BY timestamp DESC LIMIT :limit")
    List<InterventionEventEntity> recent(int limit);

    @Query("SELECT * FROM intervention_events WHERE target = :target ORDER BY timestamp DESC LIMIT :limit")
    List<InterventionEventEntity> listForTarget(String target, int limit);

    @Query("SELECT * FROM intervention_events WHERE target = :target ORDER BY timestamp DESC LIMIT 1")
    InterventionEventEntity findLatestForTarget(String target);

    @Query("UPDATE intervention_events SET followThrough = :followThrough WHERE id = :id")
    void updateFollowThrough(long id, String followThrough);

    @Query("UPDATE intervention_events SET outcome = :outcome, resolvedAt = :resolvedAt WHERE id = :id")
    void updateOutcome(long id, String outcome, long resolvedAt);
}
