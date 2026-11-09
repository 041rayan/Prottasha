package com.prottasha.identity.data;

import androidx.lifecycle.LiveData;

import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;

public final class IdentityRepository {

    private final UserSessionDatabaseProvider databaseProvider;

    public IdentityRepository(UserSessionDatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    public LiveData<IdentityEntity> observeActive() {
        return databaseProvider.requireOpen().identityDao().observeActive();
    }

    public IdentityEntity findActive() {
        return databaseProvider.requireOpen().identityDao().findActive();
    }

    public long saveActive(String statement, String purpose) {
        UserDatabase database = databaseProvider.requireOpen();
        final long[] id = new long[1];
        database.runInTransaction(() -> {
            database.identityDao().deactivateAll();
            id[0] = database.identityDao().insert(new IdentityEntity(
                    0L, statement.trim(), purpose.trim(), System.currentTimeMillis(), true));
        });
        return id[0];
    }
}
