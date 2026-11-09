package com.prottasha.focus.ui;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.ViewModel;

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
}
