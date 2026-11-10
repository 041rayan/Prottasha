package com.prottasha;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.prottasha.core.AppGraph;

public class ProttashaApp extends Application {

    public static final String FOCUS_NOTIFICATION_CHANNEL_ID = "prottasha_focus_channel";
    private AppGraph appGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        appGraph = new AppGraph(this);
        createNotificationChannels();
        setupActivityLifecycleLockTracking();
    }

    private void setupActivityLifecycleLockTracking() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private int runningActivities = 0;

            @Override
            public void onActivityStarted(android.app.Activity activity) {
                runningActivities++;
            }

            @Override
            public void onActivityStopped(android.app.Activity activity) {
                runningActivities--;
                if (runningActivities <= 0) {
                    runningActivities = 0;
                    if (appGraph != null && appGraph.appLockManager() != null) {
                        appGraph.appLockManager().onBackground(System.currentTimeMillis());
                    }
                }
            }

            @Override public void onActivityCreated(android.app.Activity a, android.os.Bundle b) {}
            @Override public void onActivityResumed(android.app.Activity a) {}
            @Override public void onActivityPaused(android.app.Activity a) {}
            @Override public void onActivitySaveInstanceState(android.app.Activity a, android.os.Bundle b) {}
            @Override public void onActivityDestroyed(android.app.Activity a) {}
        });
    }

    public AppGraph appGraph() {
        return appGraph;
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
