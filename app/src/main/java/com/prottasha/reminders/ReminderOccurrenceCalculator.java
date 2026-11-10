package com.prottasha.reminders;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

public final class ReminderOccurrenceCalculator {

    private ReminderOccurrenceCalculator() {
    }

    public static Instant next(
            Instant now,
            ZoneId zone,
            LocalTime requestedTime,
            Set<DayOfWeek> selectedDays,
            LocalTime quietStart,
            LocalTime quietEnd) {
        LocalDate start = now.atZone(zone).toLocalDate();
        for (int offset = 0; offset < 15; offset++) {
            LocalDate date = start.plusDays(offset);
            if (!selectedDays.isEmpty() && !selectedDays.contains(date.getDayOfWeek())) {
                continue;
            }
            ZonedDateTime candidate = ZonedDateTime.of(date, requestedTime, zone);
            if (!candidate.toInstant().isAfter(now)) {
                continue;
            }
            if (quietStart != null && quietEnd != null
                    && isQuiet(candidate.toLocalTime(), quietStart, quietEnd)) {
                LocalDate quietEndDate = candidate.toLocalDate();
                if (quietStart.isAfter(quietEnd)
                        && !candidate.toLocalTime().isBefore(quietStart)) {
                    quietEndDate = quietEndDate.plusDays(1);
                }
                candidate = ZonedDateTime.of(quietEndDate, quietEnd, zone);
            }
            return candidate.toInstant();
        }
        throw new IllegalStateException("No reminder occurrence found");
    }

    public static Instant snooze(Instant now, int minutes) {
        if (now == null) {
            throw new NullPointerException("now is required");
        }
        return now.plusSeconds(Math.max(1, minutes) * 60L);
    }

    private static boolean isQuiet(LocalTime time, LocalTime start, LocalTime end) {
        if (start.equals(end)) {
            return false;
        }
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        }
        return !time.isBefore(start) || time.isBefore(end);
    }
}
