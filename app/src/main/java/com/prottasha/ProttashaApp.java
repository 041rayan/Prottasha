package com.prottasha;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ProttashaApp extends Application {

    public static final String FOCUS_NOTIFICATION_CHANNEL_ID = "prottasha_focus_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel focusChannel = new NotificationChannel(
                    FOCUS_NOTIFICATION_CHANNEL_ID,
                    "Focus & Identity Sessions",
                    NotificationManager.IMPORTANCE_LOW
            );
            focusChannel.setDescription("Live progress notification during active focus and sleep mode");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(focusChannel);
            }
        }
    }
}
