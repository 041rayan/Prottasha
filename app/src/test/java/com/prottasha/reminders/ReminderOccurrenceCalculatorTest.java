package com.prottasha.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.EnumSet;

import org.junit.Test;

public final class ReminderOccurrenceCalculatorTest {

    @Test
    public void dailyReminderChoosesTodayOrTomorrow() {
        ZoneId zone = ZoneId.of("Asia/Dhaka");
        Instant morning = ZonedDateTime.of(2026, 11, 10, 8, 0, 0, 0, zone).toInstant();
        Instant evening = ZonedDateTime.of(2026, 11, 10, 10, 0, 0, 0, zone).toInstant();

        assertEquals(ZonedDateTime.of(2026, 11, 10, 9, 0, 0, 0, zone).toInstant(),
                ReminderOccurrenceCalculator.next(
                        morning, zone, LocalTime.of(9, 0), Collections.emptySet(), null, null));
        assertEquals(ZonedDateTime.of(2026, 11, 11, 9, 0, 0, 0, zone).toInstant(),
                ReminderOccurrenceCalculator.next(
                        evening, zone, LocalTime.of(9, 0), Collections.emptySet(), null, null));
    }

    @Test
    public void selectedWeekdaysSkipOtherDays() {
        ZoneId zone = ZoneId.of("UTC");
        Instant mondayAfter = ZonedDateTime.of(2026, 11, 9, 10, 0, 0, 0, zone).toInstant();

        assertEquals(DayOfWeek.WEDNESDAY,
                ReminderOccurrenceCalculator.next(
                        mondayAfter, zone, LocalTime.of(9, 0),
                        EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), null, null)
                        .atZone(zone).getDayOfWeek());
    }

    @Test
    public void springGapUsesFirstValidLocalTime() {
        ZoneId zone = ZoneId.of("America/New_York");
        Instant beforeGap = ZonedDateTime.of(2024, 3, 9, 23, 0, 0, 0, zone).toInstant();

        ZonedDateTime result = ReminderOccurrenceCalculator.next(
                beforeGap, zone, LocalTime.of(2, 30), Collections.emptySet(), null, null)
                .atZone(zone);

        assertEquals(3, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    public void crossMidnightQuietHoursMoveReminderToQuietEnd() {
        ZoneId zone = ZoneId.of("UTC");
        Instant now = ZonedDateTime.of(2026, 11, 10, 20, 0, 0, 0, zone).toInstant();

        ZonedDateTime result = ReminderOccurrenceCalculator.next(
                now, zone, LocalTime.of(23, 0), Collections.emptySet(),
                LocalTime.of(22, 0), LocalTime.of(7, 0)).atZone(zone);

        assertEquals(7, result.getHour());
        assertTrue(result.toLocalDate().isAfter(now.atZone(zone).toLocalDate()));
    }

    @Test
    public void snoozeAddsSpecifiedMinutes() {
        Instant now = Instant.ofEpochMilli(1000000L);
        assertEquals(Instant.ofEpochMilli(1000000L + (15 * 60 * 1000L)),
                ReminderOccurrenceCalculator.snooze(now, 15));
        assertEquals(Instant.ofEpochMilli(1000000L + (60 * 1000L)),
                ReminderOccurrenceCalculator.snooze(now, 0)); // Minimum 1 minute
    }
}
