package com.prottasha.guard.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GuardRuleDao {

    @Query("SELECT * FROM guard_rules ORDER BY label")
    List<GuardRuleEntity> list();

    @Query("SELECT * FROM guard_rules WHERE enabled = 1")
    List<GuardRuleEntity> listEnabled();

    @Query("SELECT * FROM guard_rules WHERE id = :id")
    GuardRuleEntity find(long id);

    @Query("SELECT * FROM guard_rules WHERE target = :target LIMIT 1")
    GuardRuleEntity findByTarget(String target);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long upsert(GuardRuleEntity rule);

    @Query("UPDATE guard_rules SET enabled = :enabled WHERE id = :id")
    void setEnabled(long id, boolean enabled);

    @Query("DELETE FROM guard_rules WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM guard_schedules WHERE ruleId = :ruleId")
    List<GuardScheduleEntity> listSchedules(long ruleId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertSchedule(GuardScheduleEntity schedule);

    @Query("DELETE FROM guard_schedules WHERE ruleId = :ruleId")
    void deleteSchedules(long ruleId);
}
