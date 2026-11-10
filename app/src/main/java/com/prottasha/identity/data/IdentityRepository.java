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

    public java.util.List<SuccessMemoryEntity> listSuccessMemories() {
        return databaseProvider.requireOpen().identityDao().listAllMemories();
    }

    public long saveSuccessMemory(String text) {
        if (text == null || text.trim().isEmpty()) return -1L;
        UserDatabase database = databaseProvider.requireOpen();
        final long[] memoryId = new long[1];
        database.runInTransaction(() -> {
            IdentityEntity active = database.identityDao().findActive();
            long identityId;
            if (active == null) {
                identityId = database.identityDao().insert(new IdentityEntity(
                        0L, "Mindful intentional user", "Build sustainable focus",
                        System.currentTimeMillis(), true));
            } else {
                identityId = active.id;
            }
            memoryId[0] = database.identityDao().insertSuccessMemory(new SuccessMemoryEntity(
                    0L, identityId, text.trim(), System.currentTimeMillis()));
        });
        return memoryId[0];
    }

    public void deleteSuccessMemory(long id) {
        databaseProvider.requireOpen().identityDao().deleteMemoryById(id);
    }
}
