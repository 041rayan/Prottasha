package com.prottasha.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prottasha.ProttashaApp;
import com.prottasha.habits.data.HabitReminderEntity;

public final class ReminderRescheduleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pending = goAsync();
        ProttashaApp app = (ProttashaApp) context.getApplicationContext();
        app.appGraph().executors().disk().execute(() -> {
            try {
                String hash = app.appGraph().userSessionDatabaseProvider()
                        .verifiedBackgroundOwnerHashOrNull();
                if (hash == null) return;
                app.appGraph().userSessionDatabaseProvider().openForVerifiedBackgroundOwner(hash);
                ReminderScheduler scheduler = new ReminderScheduler(context);
                for (HabitReminderEntity reminder : app.appGraph().userSessionDatabaseProvider()
                        .requireOpen().habitDao().listEnabledReminders()) {
                    scheduler.schedule(hash, reminder);
                }
            } catch (Exception ignored) {
                // Signed-out or unavailable owner has nothing to reschedule.
            } finally {
                app.appGraph().userSessionDatabaseProvider().close();
                pending.finish();
            }
        });
    }
}
