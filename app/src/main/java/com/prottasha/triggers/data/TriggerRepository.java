package com.prottasha.triggers.data;

import com.prottasha.data.local.user.UserSessionDatabaseProvider;

import java.util.List;

public final class TriggerRepository {
    private final UserSessionDatabaseProvider provider;

    public TriggerRepository(UserSessionDatabaseProvider provider) {
        this.provider = provider;
    }

    public List<TriggerPlanEntity> list() {
        return provider.requireOpen().triggerPlanDao().list();
    }

    public long save(String cue, String plan) {
        if (cue == null || plan == null) return -1L;
        return provider.requireOpen().triggerPlanDao().insert(new TriggerPlanEntity(
                0L, cue.trim(), plan.trim(), System.currentTimeMillis()));
    }

    public void delete(long id) {
        provider.requireOpen().triggerPlanDao().deleteById(id);
    }
}
