package com.prottasha.focus.data;

import com.prottasha.data.local.user.UserSessionDatabaseProvider;

public final class FocusRepository {
    private final UserSessionDatabaseProvider provider;

    public FocusRepository(UserSessionDatabaseProvider provider) {
        this.provider = provider;
    }

    public FocusSessionEntity find() { return provider.requireOpen().focusDao().find(); }
    public void save(FocusSessionEntity session) { provider.requireOpen().focusDao().save(session); }
    public void clear() { provider.requireOpen().focusDao().clear(); }
}
