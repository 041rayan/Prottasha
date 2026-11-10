package com.prottasha.focus.ui;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.prottasha.ProttashaApp;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;
import com.prottasha.focus.data.FocusSessionEntity;
import com.prottasha.service.ProttashaFocusService;

public final class FocusViewModel extends ViewModel {

    public void start(Context context, int minutes) {
        Intent intent = new Intent(context, ProttashaFocusService.class)
                .setAction(ProttashaFocusService.ACTION_START)
                .putExtra(ProttashaFocusService.EXTRA_DURATION_MILLIS,
                        Math.max(1, minutes) * 60_000L);
        context.startForegroundService(intent);
    }

    public void command(Context context, String action) {
        context.startService(new Intent(context, ProttashaFocusService.class).setAction(action));
    }

    public void loadSession(ProttashaApp app, @Nullable ResultCallback<FocusSessionEntity> callback) {
        app.appGraph().executors().disk().execute(() -> {
            try {
                if (app.appGraph().userSessionDatabaseProvider().isOpen()) {
                    FocusSessionEntity session = app.appGraph().userSessionDatabaseProvider()
                            .requireOpen().focusDao().find();
                    if (callback != null) callback.onResult(AppResult.success(session));
                } else {
                    if (callback != null) callback.onResult(AppResult.success(null));
                }
            } catch (Exception error) {
                if (callback != null) callback.onResult(AppResult.failure(error));
            }
        });
    }
}
