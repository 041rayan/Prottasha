package com.prottasha.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.prottasha.data.local.AppDatabase;
import com.prottasha.data.local.dao.HabitDao;
import com.prottasha.data.local.entity.HabitEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRepository {

    private final HabitDao habitDao;
    private final ExecutorService executorService;

    public HabitRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.habitDao = db.habitDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<HabitEntity>> getAllHabits() {
        return habitDao.getAllHabits();
    }

    public LiveData<List<HabitEntity>> getHabitsByType(boolean isGood) {
        return habitDao.getHabitsByType(isGood);
    }

    public LiveData<HabitEntity> getHabitById(long id) {
        return habitDao.getHabitById(id);
    }

    public void insert(HabitEntity habit) {
        executorService.execute(() -> habitDao.insert(habit));
    }

    public void update(HabitEntity habit) {
        executorService.execute(() -> habitDao.update(habit));
    }

    public void delete(HabitEntity habit) {
        executorService.execute(() -> habitDao.delete(habit));
    }

    public void deleteById(long id) {
        executorService.execute(() -> habitDao.deleteById(id));
    }

    public void recordPositiveVote(long id) {
        executorService.execute(() -> habitDao.recordPositiveVote(id, System.currentTimeMillis()));
    }

    public void recordMiss(long id) {
        executorService.execute(() -> habitDao.recordMiss(id, System.currentTimeMillis()));
    }
}
