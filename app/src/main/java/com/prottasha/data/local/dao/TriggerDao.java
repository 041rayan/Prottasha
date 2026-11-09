package com.prottasha.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.prottasha.data.local.entity.TriggerEntity;
import com.prottasha.data.local.entity.UrgeLogEntity;
import java.util.List;

@Dao
public interface TriggerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TriggerEntity trigger);

    @Update
    void update(TriggerEntity trigger);

    @Delete
    void delete(TriggerEntity trigger);

    @Query("DELETE FROM triggers WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM triggers WHERE id = :id LIMIT 1")
    TriggerEntity getTriggerByIdSync(long id);

    @Query("SELECT * FROM triggers WHERE id = :id LIMIT 1")
    LiveData<TriggerEntity> getTriggerById(long id);

    @Query("SELECT * FROM triggers ORDER BY id DESC")
    LiveData<List<TriggerEntity>> getAllTriggers();

    @Query("UPDATE triggers SET timesEncountered = timesEncountered + 1, timesOvercome = timesOvercome + (CASE WHEN :overcome THEN 1 ELSE 0 END) WHERE id = :id")
    void recordEncounter(long id, boolean overcome);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUrgeLog(UrgeLogEntity urgeLog);

    @Query("SELECT * FROM urge_logs ORDER BY timestamp DESC")
    LiveData<List<UrgeLogEntity>> getAllUrgeLogs();

    @Query("SELECT COUNT(*) FROM urge_logs WHERE resisted = 1")
    LiveData<Integer> getResistedUrgesCount();
}
