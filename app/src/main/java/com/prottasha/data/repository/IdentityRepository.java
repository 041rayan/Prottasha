package com.prottasha.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.prottasha.data.local.AppDatabase;
import com.prottasha.data.local.dao.IdentityDao;
import com.prottasha.data.local.entity.IdentityEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdentityRepository {

    private final IdentityDao identityDao;
    private final ExecutorService executorService;

    public IdentityRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.identityDao = db.identityDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<IdentityEntity> getActiveIdentity() {
        return identityDao.getActiveIdentity();
    }

    public LiveData<List<IdentityEntity>> getAllIdentities() {
        return identityDao.getAllIdentities();
    }

    public void insert(IdentityEntity identity, OnInsertedCallback callback) {
        executorService.execute(() -> {
            long id = identityDao.insert(identity);
            if (callback != null) {
                callback.onInserted(id);
            }
        });
    }

    public void update(IdentityEntity identity) {
        executorService.execute(() -> identityDao.update(identity));
    }

    public interface OnInsertedCallback {
        void onInserted(long id);
    }
}
