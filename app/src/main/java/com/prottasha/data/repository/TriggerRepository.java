package com.prottasha.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.prottasha.data.local.AppDatabase;
import com.prottasha.data.local.dao.TriggerDao;
import com.prottasha.data.local.entity.TriggerEntity;
import com.prottasha.data.local.entity.UrgeLogEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TriggerRepository {

    private final TriggerDao triggerDao;
    private final ExecutorService executorService;

    public TriggerRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.triggerDao = db.triggerDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<TriggerEntity>> getAllTriggers() {
        return triggerDao.getAllTriggers();
    }

    public LiveData<TriggerEntity> getTriggerById(long id) {
        return triggerDao.getTriggerById(id);
    }

    public void insert(TriggerEntity trigger) {
        executorService.execute(() -> triggerDao.insert(trigger));
    }

    public void update(TriggerEntity trigger) {
        executorService.execute(() -> triggerDao.update(trigger));
    }

    public void deleteById(long id) {
        executorService.execute(() -> triggerDao.deleteById(id));
    }

    public void recordEncounter(long id, boolean overcome) {
        executorService.execute(() -> triggerDao.recordEncounter(id, overcome));
    }

    public void logUrge(UrgeLogEntity urgeLog) {
        executorService.execute(() -> triggerDao.insertUrgeLog(urgeLog));
    }

    public LiveData<List<UrgeLogEntity>> getAllUrgeLogs() {
        return triggerDao.getAllUrgeLogs();
    }

    public LiveData<Integer> getResistedUrgesCount() {
        return triggerDao.getResistedUrgesCount();
    }
}
