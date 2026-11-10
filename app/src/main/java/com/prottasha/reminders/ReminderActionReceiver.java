package com.prottasha.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.prottasha.ProttashaApp;
import com.prottasha.auth.domain.SessionAccessPolicy;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.widget.TodayWidgetProvider;

import java.time.LocalDate;

public final class ReminderActionReceiver extends BroadcastReceiver {

    public static final String EXTRA_HABIT_ID = "habit_id";
    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String EXTRA_NONCE = "nonce";
    public static final String EXTRA_ACTION = "action";

    public static final String ACTION_DONE = "action_done";
    public static final String ACTION_ADD = "action_add";
    public static final String ACTION_SNOOZE = "action_snooze";

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pending = goAsync();
        ProttashaApp app = (ProttashaApp) context.getApplicationContext();
        app.appGraph().executors().disk().execute(() -> {
            boolean actionQueued = false;
            boolean wasAlreadyOpen = app.appGraph().userSessionDatabaseProvider().isOpen();
            try {
                String hash = intent.getStringExtra(ReminderScheduler.EXTRA_UID_HASH);
                String actionType = intent.getStringExtra(EXTRA_ACTION);
                long habitId = intent.getLongExtra(EXTRA_HABIT_ID, 0L);
                long reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, 0L);
                String nonce = intent.getStringExtra(EXTRA_NONCE);

                if (hash == null || habitId == 0L) {
                    return;
                }

                // Handle Snooze
                if (ACTION_SNOOZE.equals(actionType)) {
                    NotificationManagerCompat.from(context).cancel(Long.hashCode(habitId));
                    if (reminderId > 0) {
                        new ReminderScheduler(context).scheduleSnooze(hash, reminderId, 15);
                    }
                    return;
                }

                if (nonce == null) {
                    return;
                }

                if (!wasAlreadyOpen) {
                    app.appGraph().userSessionDatabaseProvider().openForVerifiedBackgroundOwner(hash);
                }
                HabitEntity habit = app.appGraph().userSessionDatabaseProvider()
                        .requireOpen().habitDao().find(habitId);

                // Avoid habits require unlock and are never mutated silently from notifications or widgets
                if (habit == null || "AVOID".equals(habit.type)) {
                    return;
                }

                SessionAccessPolicy.Action policy = "AMOUNT".equals(habit.type)
                        ? SessionAccessPolicy.Action.INCREMENT_AMOUNT
                        : SessionAccessPolicy.Action.COMPLETE_BUILD;
                if (!SessionAccessPolicy.canRun(true, false, policy)) {
                    return;
                }

                int incrementAmount = habit.incrementVal > 0 ? habit.incrementVal : 1;
                HabitAction action = "AMOUNT".equals(habit.type)
                        ? HabitAction.increment(nonce, incrementAmount)
                        : HabitAction.complete(nonce);

                app.appGraph().habitRepository().record(
                        action.forHabit(habitId, LocalDate.now().toEpochDay()), result -> {
                            if (!wasAlreadyOpen) {
                                app.appGraph().userSessionDatabaseProvider().close();
                            }
                            NotificationManagerCompat.from(context).cancel(Long.hashCode(habitId));
                            TodayWidgetProvider.updateAllWidgets(context);
                            finish(pending);
                        });
                actionQueued = true;
            } catch (Exception ignored) {
                // Invalid owner, action, or unavailable key fails closed.
            } finally {
                if (!actionQueued) {
                    if (!wasAlreadyOpen) {
                        app.appGraph().userSessionDatabaseProvider().close();
                    }
                    finish(pending);
                }
            }
        });
    }

    private static void finish(PendingResult pending) {
        if (pending != null) {
            pending.finish();
        }
    }
}
