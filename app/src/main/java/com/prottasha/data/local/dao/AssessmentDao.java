package com.prottasha.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prottasha.data.local.entity.AssessmentResultEntity;
import java.util.List;

@Dao
public interface AssessmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AssessmentResultEntity assessment);

    @Query("SELECT * FROM assessment_results ORDER BY timestamp DESC")
    LiveData<List<AssessmentResultEntity>> getAllAssessments();

    @Query("SELECT * FROM assessment_results WHERE instrumentType = :type ORDER BY timestamp DESC LIMIT 1")
    AssessmentResultEntity getLatestAssessmentSync(String type);
}
