package com.prottasha.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.prottasha.habits.data.HabitReminderEntity;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class ReminderScheduler {

    public static final String EXTRA_UID_HASH = "uid_hash";
    public static final String EXTRA_REMINDER_ID = "reminder_id";

    private final Context context;
    private final AlarmManager alarmManager;

    public ReminderScheduler(Context context) {
        this.context = context.getApplicationContext();
        alarmManager = context.getSystemService(AlarmManager.class);
    }

    public void schedule(String uidHash, HabitReminderEntity reminder) {
        if (alarmManager == null || !reminder.enabled) return;
        Instant next = ReminderOccurrenceCalculator.next(
                Instant.now(), ZoneId.systemDefault(), LocalTime.of(reminder.hour, reminder.minute),
                days(reminder.dayMask), LocalTime.of(22, 0), LocalTime.of(7, 0));
        setAlarm(next.toEpochMilli(), pending(uidHash, reminder.id));
    }

    public void scheduleSnooze(String uidHash, long reminderId, int minutes) {
        if (alarmManager == null) return;
        long triggerAt = ReminderOccurrenceCalculator.snooze(Instant.now(), minutes).toEpochMilli();
        setAlarm(triggerAt, pending(uidHash, reminderId));
    }

    public void cancel(long reminderId) {
        if (alarmManager == null) return;
        alarmManager.cancel(pending(null, reminderId));
    }

    private void setAlarm(long triggerAtMillis, PendingIntent operation) {
        if (alarmManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        }
    }

    private PendingIntent pending(String uidHash, long reminderId) {
        Intent intent = new Intent(context, ReminderAlarmReceiver.class)
                .putExtra(EXTRA_UID_HASH, uidHash)
                .putExtra(EXTRA_REMINDER_ID, reminderId);
        return PendingIntent.getBroadcast(context, Long.hashCode(reminderId), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static Set<DayOfWeek> days(int mask) {
        if (mask == 0) return Collections.emptySet();
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            if ((mask & (1 << (day.getValue() - 1))) != 0) days.add(day);
        }
        return days;
    }
}
