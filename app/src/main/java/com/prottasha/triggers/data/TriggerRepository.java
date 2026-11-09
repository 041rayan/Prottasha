package com.prottasha.triggers.data;

import com.prottasha.data.local.user.UserSessionDatabaseProvider;

public final class TriggerRepository {
    private final UserSessionDatabaseProvider provider;
    public TriggerRepository(UserSessionDatabaseProvider provider) { this.provider = provider; }
    public long save(String cue, String plan) {
        return provider.requireOpen().triggerPlanDao().insert(new TriggerPlanEntity(
                0L, cue, plan, System.currentTimeMillis()));
    }
}
