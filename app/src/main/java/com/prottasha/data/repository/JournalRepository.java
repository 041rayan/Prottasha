package com.prottasha.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.prottasha.data.local.AppDatabase;
import com.prottasha.data.local.dao.JournalDao;
import com.prottasha.data.local.entity.JournalEntryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JournalRepository {

    private final JournalDao journalDao;
    private final ExecutorService executorService;

    public JournalRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.journalDao = db.journalDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<JournalEntryEntity>> getAllEntries() {
        return journalDao.getAllEntries();
    }

    public LiveData<List<String>> getRecentSuccessMemories() {
        return journalDao.getRecentSuccessMemories();
    }

    public void insert(JournalEntryEntity entry) {
        executorService.execute(() -> journalDao.insert(entry));
    }
}
