package com.prottasha.guard.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public final class GuardRuleRepository {
    private final UserSessionDatabaseProvider provider;
    private final Executor disk;
    private final MutableLiveData<List<GuardRuleEntity>> rules =
            new MutableLiveData<>(Collections.emptyList());

    public GuardRuleRepository(UserSessionDatabaseProvider provider, Executor disk) {
        this.provider = provider;
        this.disk = disk;
    }

    public LiveData<List<GuardRuleEntity>> observeRules() {
        refresh();
        return rules;
    }

    public void save(GuardRuleEntity rule, ResultCallback<Long> callback) {
        disk.execute(() -> {
            try {
                long id = provider.requireOpen().guardRuleDao().upsert(rule);
                refresh();
                callback.onResult(AppResult.success(id));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void delete(long id) {
        disk.execute(() -> {
            provider.requireOpen().guardRuleDao().delete(id);
            refresh();
        });
    }

    private void refresh() {
        disk.execute(() -> {
            try {
                rules.postValue(provider.requireOpen().guardRuleDao().list());
            } catch (IllegalStateException staleSession) {
                // Account switch disposed this user scope.
            }
        });
    }
}
