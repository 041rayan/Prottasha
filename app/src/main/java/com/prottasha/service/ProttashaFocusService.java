package com.prottasha.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.focus.data.FocusDao;
import com.prottasha.focus.data.FocusSessionEntity;
import com.prottasha.focus.domain.FocusSessionEngine;
import com.prottasha.focus.ui.FocusActivity;

import java.util.Locale;

public final class ProttashaFocusService extends Service {
    public static final String ACTION_START = "com.prottasha.focus.START";
    public static final String ACTION_PAUSE = "com.prottasha.focus.PAUSE";
    public static final String ACTION_RESUME = "com.prottasha.focus.RESUME";
    public static final String ACTION_END = "com.prottasha.focus.END";
    public static final String EXTRA_DURATION_MILLIS = "duration_millis";
    private static final int NOTIFICATION_ID = 1001;

    private static volatile boolean sRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable ticker;
    private FocusSessionEngine engine;
    private String currentState = "IDLE";

    public static boolean isRunning() {
        return sRunning;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_NOT_STICKY;
        ProttashaApp app = (ProttashaApp) getApplication();
        String action = intent.getAction();

        if (ACTION_END.equals(action)) {
            stopTicker();
            sRunning = false;
            currentState = "IDLE";
            app.appGraph().executors().disk().execute(() -> {
                try {
                    app.appGraph().userSessionDatabaseProvider().requireOpen().focusDao().clear();
                } catch (IllegalStateException ignored) {}
            });
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        long now = System.currentTimeMillis();
        sRunning = true;

        if (ACTION_START.equals(action)) {
            long duration = intent.getLongExtra(EXTRA_DURATION_MILLIS, 25 * 60_000L);
            engine = FocusSessionEngine.start(duration, now);
            currentState = "RUNNING";
            startTicker();
            app.appGraph().executors().disk().execute(() -> {
                try {
                    FocusDao dao = app.appGraph().userSessionDatabaseProvider().requireOpen().focusDao();
                    dao.save(new FocusSessionEntity(1, duration, now, 0L, "RUNNING"));
                } catch (IllegalStateException ignored) {}
            });
        } else if (ACTION_PAUSE.equals(action)) {
            if (engine != null) engine.pause(now);
            currentState = "PAUSED";
            stopTicker();
            app.appGraph().executors().disk().execute(() -> {
                try {
                    FocusDao dao = app.appGraph().userSessionDatabaseProvider().requireOpen().focusDao();
                    FocusSessionEntity current = dao.find();
                    if (current != null) {
                        dao.save(new FocusSessionEntity(1, current.durationMillis, current.startedAt,
                                current.accumulatedMillis + Math.max(0, now - current.startedAt), "PAUSED"));
                    }
                } catch (IllegalStateException ignored) {}
            });
        } else if (ACTION_RESUME.equals(action)) {
            if (engine != null) engine.resume(now);
            currentState = "RUNNING";
            startTicker();
            app.appGraph().executors().disk().execute(() -> {
                try {
                    FocusDao dao = app.appGraph().userSessionDatabaseProvider().requireOpen().focusDao();
                    FocusSessionEntity current = dao.find();
                    if (current != null) {
                        dao.save(new FocusSessionEntity(1, current.durationMillis, now,
                                current.accumulatedMillis, "RUNNING"));
                    }
                } catch (IllegalStateException ignored) {}
            });
        }

        startForeground(NOTIFICATION_ID, buildNotification(currentState, remainingSeconds()));
        return START_NOT_STICKY;
    }

    private int remainingSeconds() {
        if (engine == null) return 0;
        return (int) (engine.remainingAt(System.currentTimeMillis()) / 1000L);
    }

    private void startTicker() {
        stopTicker();
        ticker = new Runnable() {
            @Override
            public void run() {
                if (!sRunning || !"RUNNING".equals(currentState)) return;
                long now = System.currentTimeMillis();
                if (engine != null && engine.isCompleteAt(now)) {
                    currentState = "COMPLETED";
                    stopTicker();
                    updateNotification(currentState, 0);
                    return;
                }
                updateNotification(currentState, remainingSeconds());
                handler.postDelayed(this, 1000L);
            }
        };
        handler.postDelayed(ticker, 1000L);
    }

    private void stopTicker() {
        if (ticker != null) {
            handler.removeCallbacks(ticker);
            ticker = null;
        }
    }

    private void updateNotification(String state, int secondsLeft) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification(state, secondsLeft));
        }
    }

    private Notification buildNotification(String state, int secondsLeft) {
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, FocusActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int mins = secondsLeft / 60;
        int secs = secondsLeft % 60;
        String contentText;
        if ("PAUSED".equals(state)) {
            contentText = String.format(Locale.ROOT, "Paused at %02d:%02d", mins, secs);
        } else if ("COMPLETED".equals(state)) {
            contentText = "Focus session completed! Great work.";
        } else {
            contentText = String.format(Locale.ROOT, "%02d:%02d remaining", mins, secs);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, ProttashaApp.FOCUS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.focus_notification_title))
                .setContentText(contentText)
                .setContentIntent(open)
                .setOngoing(!"COMPLETED".equals(state));

        // Add action buttons
        if ("RUNNING".equals(state)) {
            PendingIntent pauseIntent = PendingIntent.getService(this, 1,
                    new Intent(this, ProttashaFocusService.class).setAction(ACTION_PAUSE),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(0, "Pause", pauseIntent);

            PendingIntent endIntent = PendingIntent.getService(this, 2,
                    new Intent(this, ProttashaFocusService.class).setAction(ACTION_END),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(0, "End", endIntent);
        } else if ("PAUSED".equals(state)) {
            PendingIntent resumeIntent = PendingIntent.getService(this, 3,
                    new Intent(this, ProttashaFocusService.class).setAction(ACTION_RESUME),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(0, "Resume", resumeIntent);

            PendingIntent endIntent = PendingIntent.getService(this, 2,
                    new Intent(this, ProttashaFocusService.class).setAction(ACTION_END),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(0, "End", endIntent);
        }

        return builder.build();
    }

    @Override
    public void onDestroy() {
        stopTicker();
        sRunning = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
