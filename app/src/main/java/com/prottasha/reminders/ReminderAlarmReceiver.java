package com.prottasha.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prottasha.ProttashaApp;
import com.prottasha.habits.data.HabitReminderEntity;

public final class ReminderAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pending = goAsync();
        ProttashaApp app = (ProttashaApp) context.getApplicationContext();
        app.appGraph().executors().disk().execute(() -> {
            try {
                String hash = intent.getStringExtra(ReminderScheduler.EXTRA_UID_HASH);
                long reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, 0L);
                app.appGraph().userSessionDatabaseProvider().openForVerifiedBackgroundOwner(hash);
                HabitReminderEntity reminder = app.appGraph().userSessionDatabaseProvider()
                        .requireOpen().habitDao().findReminder(reminderId);
                if (reminder != null) {
                    new NotificationController(context).show(hash,
                            app.appGraph().userSessionDatabaseProvider().requireOpen()
                                    .habitDao().find(reminder.habitId));
                    new ReminderScheduler(context).schedule(hash, reminder);
                }
            } catch (Exception ignored) {
                // Stale, signed-out, or key-unavailable reminders fail closed.
            } finally {
                app.appGraph().userSessionDatabaseProvider().close();
                pending.finish();
            }
        });
    }
}
