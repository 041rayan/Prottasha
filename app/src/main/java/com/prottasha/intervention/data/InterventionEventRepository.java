package com.prottasha.intervention.data;

import com.prottasha.data.local.user.UserSessionDatabaseProvider;

import java.util.concurrent.Executor;

public final class InterventionEventRepository {
    private final UserSessionDatabaseProvider provider;
    private final Executor disk;

    public InterventionEventRepository(UserSessionDatabaseProvider provider, Executor disk) {
        this.provider = provider;
        this.disk = disk;
    }

    public void record(String target, String type, String intention) {
        disk.execute(() -> {
            try {
                provider.requireOpen().interventionDao().insert(new InterventionEventEntity(
                        0L, target, type, System.currentTimeMillis(),
                        intention == null ? "" : intention));
            } catch (IllegalStateException noSession) {
                // Never fabricate events without an encrypted owner database.
            }
        });
    }
}
