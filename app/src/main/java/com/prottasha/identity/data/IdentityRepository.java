package com.prottasha.identity.data;

import androidx.lifecycle.LiveData;

import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;

public class IdentityRepository {

    private final UserSessionDatabaseProvider databaseProvider;

    public IdentityRepository(UserSessionDatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    public LiveData<IdentityEntity> observeActive() {
        return databaseProvider.requireOpen().identityDao().observeActive();
    }

    public LiveData<SuccessMemoryEntity> observeLatestSuccessMemory() {
        return databaseProvider.requireOpen().identityDao().observeLatestSuccessMemory();
    }

    public SuccessMemoryEntity findLatestSuccessMemory() {
        return databaseProvider.requireOpen().identityDao().findLatestSuccessMemory();
    }

    public IdentityEntity findActive() {
        return databaseProvider.requireOpen().identityDao().findActive();
    }

    public long saveActive(String statement, String purpose) {
        return saveActive(statement, purpose, null);
    }

    public long saveActive(String statement, String purpose, @androidx.annotation.Nullable String successMemory) {
        UserDatabase database = databaseProvider.requireOpen();
        final long[] id = new long[1];
        database.runInTransaction(() -> {
            database.identityDao().deactivateAll();
            id[0] = database.identityDao().insert(new IdentityEntity(
                    0L, statement.trim(), purpose.trim(), System.currentTimeMillis(), true));
            if (successMemory != null && !successMemory.trim().isEmpty()) {
                database.identityDao().insertSuccessMemory(new SuccessMemoryEntity(
                        0L, id[0], successMemory.trim(), System.currentTimeMillis()));
            }
        });
        return id[0];
    }
}
