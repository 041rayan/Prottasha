package com.prottasha.reminders;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.prottasha.R;
import com.prottasha.auth.ui.LaunchActivity;
import com.prottasha.habits.data.HabitEntity;

public final class NotificationController {

    private static final String CHANNEL = "habit_reminders";
    private final Context context;

    public NotificationController(Context context) {
        this.context = context.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(new NotificationChannel(
                        CHANNEL, context.getString(R.string.reminder_channel),
                        NotificationManager.IMPORTANCE_DEFAULT));
            }
        }
    }

    public void show(String uidHash, HabitEntity habit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return;
        Intent open = new Intent(context, LaunchActivity.class);
        PendingIntent content = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(habit.title)
                .setContentText(context.getString(R.string.reminder_private_body))
                .setContentIntent(content)
                .setAutoCancel(true);
        if (!"AVOID".equals(habit.type)) {
            Intent action = new Intent(context, ReminderActionReceiver.class)
                    .putExtra(ReminderScheduler.EXTRA_UID_HASH, uidHash)
                    .putExtra(ReminderActionReceiver.EXTRA_HABIT_ID, habit.id)
                    .putExtra(ReminderActionReceiver.EXTRA_NONCE,
                            "reminder-" + habit.id + '-' + System.currentTimeMillis());
            PendingIntent actionIntent = PendingIntent.getBroadcast(
                    context, Long.hashCode(habit.id), action,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(0, context.getString(R.string.reminder_action), actionIntent);
        }
        try {
            NotificationManagerCompat.from(context).notify(Long.hashCode(habit.id), builder.build());
        } catch (SecurityException ignored) {
            // Notification permission can be revoked between check and notify.
        }
    }
}
