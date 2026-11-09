package com.prottasha.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.ui.main.MainActivity;

public class ProttashaFocusService extends Service {

    private static final int NOTIFICATION_ID = 1001;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int elapsedMinutes = 0;
    private boolean isRunning = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedMinutes++;
                updateNotification();
                handler.postDelayed(this, 60000); // Update every minute
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            elapsedMinutes = 0;
            startForeground(NOTIFICATION_ID, buildNotification());
            handler.postDelayed(timerRunnable, 60000);
        }
        return START_STICKY;
    }

    private void updateNotification() {
        Notification notification = buildNotification();
        androidx.core.app.NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification);
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "Prottasha Deep Focus Active";
        String content = "Living as ideal identity • " + elapsedMinutes + " minutes distraction-free";

        return new NotificationCompat.Builder(this, ProttashaApp.FOCUS_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_tab_identity)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
