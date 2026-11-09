package com.prottasha.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prottasha.ProttashaApp;
import com.prottasha.auth.domain.SessionAccessPolicy;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.domain.HabitAction;

import java.time.LocalDate;

public final class ReminderActionReceiver extends BroadcastReceiver {
    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_NONCE = "nonce";

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pending = goAsync();
        ProttashaApp app = (ProttashaApp) context.getApplicationContext();
        app.appGraph().executors().disk().execute(() -> {
            boolean actionQueued = false;
            try {
                String hash = intent.getStringExtra(ReminderScheduler.EXTRA_UID_HASH);
                String nonce = intent.getStringExtra(EXTRA_NONCE);
                long habitId = intent.getLongExtra(EXTRA_HABIT_ID, 0L);
                if (hash == null || nonce == null || habitId == 0L) return;
                app.appGraph().userSessionDatabaseProvider().openForVerifiedBackgroundOwner(hash);
                HabitEntity habit = app.appGraph().userSessionDatabaseProvider()
                        .requireOpen().habitDao().find(habitId);
                if (habit == null || "AVOID".equals(habit.type)) return;
                SessionAccessPolicy.Action policy = "AMOUNT".equals(habit.type)
                        ? SessionAccessPolicy.Action.INCREMENT_AMOUNT
                        : SessionAccessPolicy.Action.COMPLETE_BUILD;
                if (!SessionAccessPolicy.canRun(true, false, policy)) return;
                HabitAction action = "AMOUNT".equals(habit.type)
                        ? HabitAction.increment(nonce, 1) : HabitAction.complete(nonce);
                app.appGraph().habitRepository().record(
                        action.forHabit(habitId, LocalDate.now().toEpochDay()), ignored -> {
                            app.appGraph().userSessionDatabaseProvider().close();
                            finish(pending);
                        });
                actionQueued = true;
            } catch (Exception ignored) {
                // Invalid owner, action, or unavailable key fails closed.
            } finally {
                if (!actionQueued) {
                    app.appGraph().userSessionDatabaseProvider().close();
                    finish(pending);
                }
            }
        });
    }

    private static void finish(PendingResult pending) {
        if (pending != null) pending.finish();
    }
}
