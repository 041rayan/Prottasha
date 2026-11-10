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

    public static final String CHANNEL = "habit_reminders";
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
        show(uidHash, habit, 0L);
    }

    public void show(String uidHash, HabitEntity habit, long reminderId) {
        if (habit == null) return;
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

        int requestCode = Long.hashCode(habit.id);

        if ("AVOID".equals(habit.type)) {
            // Avoid requires unlock: provide "Open" action pointing to LaunchActivity
            builder.addAction(0, context.getString(R.string.reminder_open), content);
        } else {
            // Build or Amount: provide idempotent quick action
            String actionTitle = "AMOUNT".equals(habit.type)
                    ? context.getString(R.string.reminder_add)
                    : context.getString(R.string.reminder_done);
            String actionType = "AMOUNT".equals(habit.type)
                    ? ReminderActionReceiver.ACTION_ADD
                    : ReminderActionReceiver.ACTION_DONE;

            Intent action = new Intent(context, ReminderActionReceiver.class)
                    .putExtra(ReminderScheduler.EXTRA_UID_HASH, uidHash)
                    .putExtra(ReminderActionReceiver.EXTRA_HABIT_ID, habit.id)
                    .putExtra(ReminderActionReceiver.EXTRA_ACTION, actionType)
                    .putExtra(ReminderActionReceiver.EXTRA_NONCE,
                            "reminder-" + habit.id + '-' + System.currentTimeMillis());
            PendingIntent actionIntent = PendingIntent.getBroadcast(
                    context, requestCode, action,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(0, actionTitle, actionIntent);
        }

        // Snooze action for all reminders if reminderId is provided
        if (reminderId > 0) {
            Intent snoozeIntent = new Intent(context, ReminderActionReceiver.class)
                    .putExtra(ReminderScheduler.EXTRA_UID_HASH, uidHash)
                    .putExtra(ReminderActionReceiver.EXTRA_HABIT_ID, habit.id)
                    .putExtra(ReminderActionReceiver.EXTRA_REMINDER_ID, reminderId)
                    .putExtra(ReminderActionReceiver.EXTRA_ACTION, ReminderActionReceiver.ACTION_SNOOZE);
            PendingIntent snoozePending = PendingIntent.getBroadcast(
                    context, requestCode + 1, snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(0, context.getString(R.string.reminder_snooze), snoozePending);
        }

        try {
            NotificationManagerCompat.from(context).notify(requestCode, builder.build());
        } catch (SecurityException ignored) {
            // Notification permission can be revoked between check and notify.
        }
    }
}
