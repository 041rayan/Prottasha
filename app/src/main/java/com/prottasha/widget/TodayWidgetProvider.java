package com.prottasha.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.ui.LaunchActivity;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.data.HabitRepository;
import com.prottasha.habits.data.HabitVacationEntity;
import com.prottasha.habits.domain.HabitSchedule;
import com.prottasha.habits.domain.HabitScheduleEvaluator;
import com.prottasha.reminders.ReminderActionReceiver;
import com.prottasha.reminders.ReminderScheduler;

import java.time.LocalDate;
import java.util.List;

public final class TodayWidgetProvider extends AppWidgetProvider {

    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, TodayWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(componentName);
        if (ids != null && ids.length > 0) {
            Intent updateIntent = new Intent(context, TodayWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(updateIntent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        ProttashaApp app = (ProttashaApp) context.getApplicationContext();
        app.appGraph().executors().disk().execute(() -> {
            RemoteViews views = buildViews(context, app);
            for (int widgetId : appWidgetIds) {
                manager.updateAppWidget(widgetId, views);
            }
        });
    }

    private RemoteViews buildViews(Context context, ProttashaApp app) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today);
        Intent openApp = new Intent(context, LaunchActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, openPending);

        String ownerHash = app.appGraph().userSessionDatabaseProvider().verifiedBackgroundOwnerHashOrNull();
        if (ownerHash == null) {
            views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_neutral_title));
            views.setTextViewText(R.id.widget_body, context.getString(R.string.widget_neutral_body));
            views.setViewVisibility(R.id.widget_badge, View.GONE);
            views.setViewVisibility(R.id.widget_actions_container, View.GONE);
            return views;
        }

        boolean wasAlreadyOpen = app.appGraph().userSessionDatabaseProvider().isOpen();
        try {
            if (!wasAlreadyOpen) {
                app.appGraph().userSessionDatabaseProvider().openForVerifiedBackgroundOwner(ownerHash);
            }
            UserDatabase db = app.appGraph().userSessionDatabaseProvider().requireOpen();
            long today = LocalDate.now().toEpochDay();
            List<HabitEntity> habits = db.habitDao().listActive();

            int dueCount = 0;
            int completedCount = 0;
            HabitEntity topDueActionableHabit = null;

            for (HabitEntity habit : habits) {
                HabitSchedule schedule = HabitRepository.schedule(habit);
                List<HabitVacationEntity> vacations = db.habitDao().listVacations(habit.id);
                if (!HabitScheduleEvaluator.isDue(schedule, today, null, vacations)) {
                    continue;
                }
                dueCount++;
                HabitDayAggregateEntity aggregate = db.habitDao().findAggregate(habit.id, today);
                boolean isDone = aggregate != null && aggregate.completed;
                if (isDone) {
                    completedCount++;
                } else if (topDueActionableHabit == null && !"AVOID".equals(habit.type)) {
                    topDueActionableHabit = habit;
                }
            }

            if (dueCount == 0) {
                views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_neutral_title));
                views.setTextViewText(R.id.widget_body, "No habits due today.");
                views.setViewVisibility(R.id.widget_badge, View.GONE);
                views.setViewVisibility(R.id.widget_actions_container, View.GONE);
            } else if (topDueActionableHabit != null) {
                views.setTextViewText(R.id.widget_title, topDueActionableHabit.title);
                String badgeText = completedCount + "/" + dueCount + " done";
                views.setTextViewText(R.id.widget_badge, badgeText);
                views.setViewVisibility(R.id.widget_badge, View.VISIBLE);

                String body = !topDueActionableHabit.cue.isEmpty()
                        ? topDueActionableHabit.cue
                        : "Tap to record today's progress";
                views.setTextViewText(R.id.widget_body, body);

                String actionType = "AMOUNT".equals(topDueActionableHabit.type)
                        ? ReminderActionReceiver.ACTION_ADD
                        : ReminderActionReceiver.ACTION_DONE;
                String buttonText = "AMOUNT".equals(topDueActionableHabit.type)
                        ? context.getString(R.string.reminder_add)
                        : context.getString(R.string.reminder_done);

                Intent actionIntent = new Intent(context, ReminderActionReceiver.class)
                        .putExtra(ReminderScheduler.EXTRA_UID_HASH, ownerHash)
                        .putExtra(ReminderActionReceiver.EXTRA_HABIT_ID, topDueActionableHabit.id)
                        .putExtra(ReminderActionReceiver.EXTRA_ACTION, actionType)
                        .putExtra(ReminderActionReceiver.EXTRA_NONCE,
                                "widget-" + topDueActionableHabit.id + "-" + System.currentTimeMillis());
                PendingIntent actionPending = PendingIntent.getBroadcast(
                        context, Long.hashCode(topDueActionableHabit.id), actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                views.setTextViewText(R.id.widget_action_button, buttonText);
                views.setOnClickPendingIntent(R.id.widget_action_button, actionPending);
                views.setViewVisibility(R.id.widget_actions_container, View.VISIBLE);
            } else {
                views.setTextViewText(R.id.widget_title, "All Done Today!");
                views.setTextViewText(R.id.widget_badge, completedCount + "/" + dueCount + " done");
                views.setViewVisibility(R.id.widget_badge, View.VISIBLE);
                views.setTextViewText(R.id.widget_body, "All actionable habits completed.");
                views.setViewVisibility(R.id.widget_actions_container, View.GONE);
            }
        } catch (Exception error) {
            views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_neutral_title));
            views.setTextViewText(R.id.widget_body, context.getString(R.string.widget_neutral_body));
            views.setViewVisibility(R.id.widget_badge, View.GONE);
            views.setViewVisibility(R.id.widget_actions_container, View.GONE);
        } finally {
            if (!wasAlreadyOpen) {
                app.appGraph().userSessionDatabaseProvider().close();
            }
        }

        return views;
    }
}
