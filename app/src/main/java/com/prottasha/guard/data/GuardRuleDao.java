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

    @Query("SELECT * FROM guard_rules WHERE id = :id")
    GuardRuleEntity find(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long upsert(GuardRuleEntity rule);

    @Query("DELETE FROM guard_rules WHERE id = :id")
    void delete(long id);
}
