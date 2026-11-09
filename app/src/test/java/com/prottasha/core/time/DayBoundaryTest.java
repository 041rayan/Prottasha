package com.prottasha.core.time;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.Test;

public final class DayBoundaryTest {

    @Test
    public void midnightCutoffUsesCalendarDate() {
        long day = DayBoundary.toEpochDay(
                Instant.parse("2026-11-10T00:00:00Z"), ZoneOffset.UTC, 0);

        assertEquals(LocalDate.of(2026, 11, 10).toEpochDay(), day);
    }

    @Test
    public void timeBeforeFourAmBelongsToPreviousDay() {
        ZoneId dhaka = ZoneId.of("Asia/Dhaka");

        assertEquals(LocalDate.of(2026, 11, 9).toEpochDay(), DayBoundary.toEpochDay(
                Instant.parse("2026-11-09T21:59:59Z"), dhaka, 4 * 60));
        assertEquals(LocalDate.of(2026, 11, 10).toEpochDay(), DayBoundary.toEpochDay(
                Instant.parse("2026-11-09T22:00:00Z"), dhaka, 4 * 60));
    }

    @Test
    public void springDstGapStillUsesLocalClockBoundary() {
        ZoneId newYork = ZoneId.of("America/New_York");

        assertEquals(LocalDate.of(2024, 3, 9).toEpochDay(), DayBoundary.toEpochDay(
                Instant.parse("2024-03-10T07:30:00Z"), newYork, 4 * 60));
        assertEquals(LocalDate.of(2024, 3, 10).toEpochDay(), DayBoundary.toEpochDay(
                Instant.parse("2024-03-10T08:00:00Z"), newYork, 4 * 60));
    }

    @Test
    public void bothFallDstOneThirtyInstantsBelongToPreviousDay() {
        ZoneId newYork = ZoneId.of("America/New_York");
        long expected = LocalDate.of(2024, 11, 2).toEpochDay();

        assertEquals(expected, DayBoundary.toEpochDay(
                Instant.parse("2024-11-03T05:30:00Z"), newYork, 4 * 60));
        assertEquals(expected, DayBoundary.toEpochDay(
                Instant.parse("2024-11-03T06:30:00Z"), newYork, 4 * 60));
    }

    @Test
    public void sameInstantCanBelongToDifferentLocalDays() {
        Instant instant = Instant.parse("2026-11-09T20:00:00Z");

        assertEquals(LocalDate.of(2026, 11, 9).toEpochDay(),
                DayBoundary.toEpochDay(instant, ZoneOffset.UTC, 0));
        assertEquals(LocalDate.of(2026, 11, 10).toEpochDay(),
                DayBoundary.toEpochDay(instant, ZoneId.of("Asia/Dhaka"), 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeCutoff() {
        DayBoundary.toEpochDay(Instant.EPOCH, ZoneOffset.UTC, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCutoffAfterEndOfDay() {
        DayBoundary.toEpochDay(Instant.EPOCH, ZoneOffset.UTC, 1440);
    }
}
