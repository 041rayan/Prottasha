package com.prottasha.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.focus.data.FocusSessionEntity;
import com.prottasha.focus.ui.FocusActivity;

public final class ProttashaFocusService extends Service {
    public static final String ACTION_START = "com.prottasha.focus.START";
    public static final String ACTION_PAUSE = "com.prottasha.focus.PAUSE";
    public static final String ACTION_RESUME = "com.prottasha.focus.RESUME";
    public static final String ACTION_END = "com.prottasha.focus.END";
    public static final String EXTRA_DURATION_MILLIS = "duration_millis";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_NOT_STICKY;
        ProttashaApp app = (ProttashaApp) getApplication();
        String action = intent.getAction();
        if (ACTION_END.equals(action)) {
            app.appGraph().executors().disk().execute(() -> {
                try { app.appGraph().userSessionDatabaseProvider().requireOpen().focusDao().clear(); }
                catch (IllegalStateException ignored) { }
            });
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }
        long now = System.currentTimeMillis();
        app.appGraph().executors().disk().execute(() -> {
            try {
                com.prottasha.focus.data.FocusDao dao = app.appGraph()
                        .userSessionDatabaseProvider().requireOpen().focusDao();
                FocusSessionEntity current = dao.find();
                if (ACTION_START.equals(action)) {
                    dao.save(new FocusSessionEntity(1,
                            intent.getLongExtra(EXTRA_DURATION_MILLIS, 25 * 60_000L),
                            now, 0L, "RUNNING"));
                } else if (current != null && ACTION_PAUSE.equals(action)) {
                    dao.save(new FocusSessionEntity(1, current.durationMillis, current.startedAt,
                            current.accumulatedMillis + Math.max(0, now - current.startedAt), "PAUSED"));
                } else if (current != null && ACTION_RESUME.equals(action)) {
                    dao.save(new FocusSessionEntity(1, current.durationMillis, now,
                            current.accumulatedMillis, "RUNNING"));
                }
            } catch (IllegalStateException ignored) { }
        });
        startForeground(NOTIFICATION_ID, notification());
        return START_NOT_STICKY;
    }

    private Notification notification() {
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, FocusActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, ProttashaApp.FOCUS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.focus_notification_title))
                .setContentText(getString(R.string.focus_notification_body))
                .setContentIntent(open)
                .setOngoing(true)
                .build();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
