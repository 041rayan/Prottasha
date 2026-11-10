package com.prottasha.insights.domain;

import com.prottasha.focus.data.FocusSessionEntity;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.intervention.data.InterventionEventEntity;
import com.prottasha.usage.data.UsageDailyEntity;
import com.prottasha.usage.domain.UsageAggregator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InsightsCalculator {
    public enum Outcome { STEP_BACK, RAPID_REOPEN, FOLLOW_THROUGH, CONTINUE, BLOCKED }
    public enum Range { TODAY, LAST_7_DAYS, LAST_30_DAYS }
    public enum TimeWindow { MORNING, AFTERNOON, EVENING, NIGHT }

    public static final class TargetUsageSummary {
        public final String target;
        public final long totalMillis;
        public final int opens;
        public final int sessions;
        public final UsageAggregator.Quality quality;

        public TargetUsageSummary(String target, long totalMillis, int opens, int sessions,
                UsageAggregator.Quality quality) {
            this.target = target;
            this.totalMillis = totalMillis;
            this.opens = opens;
            this.sessions = sessions;
            this.quality = quality;
        }
    }

    public static final class PatternSummary {
        public final boolean adequateSample;
        public final TimeWindow peakWindow;
        public final String topTarget;

        public PatternSummary(boolean adequateSample, TimeWindow peakWindow, String topTarget) {
            this.adequateSample = adequateSample;
            this.peakWindow = peakWindow;
            this.topTarget = topTarget;
        }
    }

    public static final class Snapshot {
        public final boolean hasData;
        public final Range range;
        public final double followThroughRate;
        public final boolean adequateSample;
        public final int decisions;
        public final int stepBackCount;
        public final int continuedCount;
        public final int rapidReopenCount;
        public final int replacementChosenCount;
        public final int focusStartedCount;
        public final int followThroughCount;
        public final long totalUsageMillis;
        public final int totalOpens;
        public final int totalSessions;
        public final UsageAggregator.Quality usageQuality;
        public final List<TargetUsageSummary> targetBreakdowns;
        public final int habitsCompleted;
        public final int habitsTotal;
        public final double habitConsistencyRate;
        public final int focusMinutes;
        public final PatternSummary patternSummary;

        public Snapshot(
                boolean hasData,
                Range range,
                double followThroughRate,
                boolean adequateSample,
                int decisions,
                int stepBackCount,
                int continuedCount,
                int rapidReopenCount,
                int replacementChosenCount,
                int focusStartedCount,
                int followThroughCount,
                long totalUsageMillis,
                int totalOpens,
                int totalSessions,
                UsageAggregator.Quality usageQuality,
                List<TargetUsageSummary> targetBreakdowns,
                int habitsCompleted,
                int habitsTotal,
                double habitConsistencyRate,
                int focusMinutes,
                PatternSummary patternSummary) {
            this.hasData = hasData;
            this.range = range;
            this.followThroughRate = followThroughRate;
            this.adequateSample = adequateSample;
            this.decisions = decisions;
            this.stepBackCount = stepBackCount;
            this.continuedCount = continuedCount;
            this.rapidReopenCount = rapidReopenCount;
            this.replacementChosenCount = replacementChosenCount;
            this.focusStartedCount = focusStartedCount;
            this.followThroughCount = followThroughCount;
            this.totalUsageMillis = totalUsageMillis;
            this.totalOpens = totalOpens;
            this.totalSessions = totalSessions;
            this.usageQuality = usageQuality;
            this.targetBreakdowns = targetBreakdowns != null ? targetBreakdowns : Collections.emptyList();
            this.habitsCompleted = habitsCompleted;
            this.habitsTotal = habitsTotal;
            this.habitConsistencyRate = habitConsistencyRate;
            this.focusMinutes = focusMinutes;
            this.patternSummary = patternSummary;
        }
    }

    private InsightsCalculator() { }

    public static Snapshot calculate(List<Outcome> outcomes) {
        if (outcomes.isEmpty()) {
            return new Snapshot(
                    false, Range.LAST_30_DAYS, 0.0, false, 0, 0, 0, 0, 0, 0, 0,
                    0L, 0, 0, UsageAggregator.Quality.COMPLETE, Collections.emptyList(),
                    0, 0, 0.0, 0, new PatternSummary(false, null, null));
        }
        int followedThrough = 0;
        int rapidReopens = 0;
        int stepBacks = 0;
        int continues = 0;
        for (Outcome outcome : outcomes) {
            if (outcome == Outcome.FOLLOW_THROUGH) followedThrough++;
            else if (outcome == Outcome.RAPID_REOPEN) rapidReopens++;
            else if (outcome == Outcome.STEP_BACK) stepBacks++;
            else if (outcome == Outcome.CONTINUE) continues++;
        }
        int decisions = outcomes.size();
        boolean adequate = decisions >= 7;
        return new Snapshot(
                true, Range.LAST_30_DAYS, followedThrough / (double) decisions, adequate,
                decisions, stepBacks, continues, rapidReopens, 0, 0, followedThrough,
                0L, 0, 0, UsageAggregator.Quality.COMPLETE, Collections.emptyList(),
                0, 0, 0.0, 0, new PatternSummary(adequate, null, null));
    }

    public static Snapshot calculate(
            Range range,
            long currentEpochDay,
            ZoneId zoneId,
            List<InterventionEventEntity> interventions,
            List<UsageDailyEntity> usageList,
            List<HabitDayAggregateEntity> habitAggregates,
            List<FocusSessionEntity> focusSessions) {

        long startEpochDay;
        if (range == Range.TODAY) {
            startEpochDay = currentEpochDay;
        } else if (range == Range.LAST_7_DAYS) {
            startEpochDay = currentEpochDay - 6;
        } else {
            startEpochDay = currentEpochDay - 29;
        }

        long startMillis = LocalDate.ofEpochDay(startEpochDay).atStartOfDay(zoneId).toInstant().toEpochMilli();
        long endMillis = LocalDate.ofEpochDay(currentEpochDay + 1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1;

        // Process Interventions
        int decisions = 0;
        int stepBackCount = 0;
        int continuedCount = 0;
        int rapidReopenCount = 0;
        int replacementChosenCount = 0;
        int focusStartedCount = 0;
        int followThroughCount = 0;

        Map<String, Integer> targetCounts = new HashMap<>();
        Map<TimeWindow, Integer> windowCounts = new HashMap<>();

        if (interventions != null) {
            for (InterventionEventEntity event : interventions) {
                if (event.timestamp < startMillis || event.timestamp > endMillis) continue;
                decisions++;
                targetCounts.put(event.target, targetCounts.getOrDefault(event.target, 0) + 1);

                ZonedDateTime eventTime = Instant.ofEpochMilli(event.timestamp).atZone(zoneId);
                int hour = eventTime.getHour();
                TimeWindow window;
                if (hour >= 6 && hour < 12) window = TimeWindow.MORNING;
                else if (hour >= 12 && hour < 18) window = TimeWindow.AFTERNOON;
                else if (hour >= 18 && hour < 22) window = TimeWindow.EVENING;
                else window = TimeWindow.NIGHT;
                windowCounts.put(window, windowCounts.getOrDefault(window, 0) + 1);

                if ("FOLLOW_THROUGH".equals(event.followThrough)) {
                    followThroughCount++;
                } else if ("RAPID_REOPEN".equals(event.followThrough)) {
                    rapidReopenCount++;
                }

                if ("STEP_BACK".equals(event.outcome)) {
                    stepBackCount++;
                } else if ("CONTINUED".equals(event.outcome)) {
                    continuedCount++;
                } else if ("REPLACEMENT_CHOSEN".equals(event.outcome)) {
                    replacementChosenCount++;
                } else if ("FOCUS_STARTED".equals(event.outcome)) {
                    focusStartedCount++;
                }
            }
        }

        // Process Usage
        long totalUsageMillis = 0L;
        int totalOpens = 0;
        int totalSessions = 0;
        UsageAggregator.Quality overallQuality = UsageAggregator.Quality.COMPLETE;
        boolean hasAnyUsageRecord = false;

        Map<String, Long> targetDuration = new HashMap<>();
        Map<String, Integer> targetOpens = new HashMap<>();
        Map<String, Integer> targetSessions = new HashMap<>();
        Map<String, UsageAggregator.Quality> targetQuality = new HashMap<>();

        if (usageList != null) {
            for (UsageDailyEntity usage : usageList) {
                if (usage.epochDay < startEpochDay || usage.epochDay > currentEpochDay) continue;
                hasAnyUsageRecord = true;
                totalUsageMillis += usage.totalMillis;
                totalOpens += usage.opens;
                totalSessions += usage.sessions;

                targetDuration.put(usage.target, targetDuration.getOrDefault(usage.target, 0L) + usage.totalMillis);
                targetOpens.put(usage.target, targetOpens.getOrDefault(usage.target, 0) + usage.opens);
                targetSessions.put(usage.target, targetSessions.getOrDefault(usage.target, 0) + usage.sessions);

                UsageAggregator.Quality q = UsageAggregator.Quality.COMPLETE;
                try { q = UsageAggregator.Quality.valueOf(usage.quality); } catch (Exception ignored) { }
                UsageAggregator.Quality existingQ = targetQuality.get(usage.target);
                if (existingQ == null || q == UsageAggregator.Quality.UNAVAILABLE
                        || (q == UsageAggregator.Quality.PARTIAL && existingQ != UsageAggregator.Quality.UNAVAILABLE)) {
                    targetQuality.put(usage.target, q);
                }

                if (q == UsageAggregator.Quality.UNAVAILABLE) {
                    overallQuality = UsageAggregator.Quality.UNAVAILABLE;
                } else if (q == UsageAggregator.Quality.PARTIAL && overallQuality != UsageAggregator.Quality.UNAVAILABLE) {
                    overallQuality = UsageAggregator.Quality.PARTIAL;
                }
            }
        }
        if (!hasAnyUsageRecord) {
            overallQuality = UsageAggregator.Quality.COMPLETE;
        }

        List<TargetUsageSummary> breakdowns = new ArrayList<>();
        for (String target : targetDuration.keySet()) {
            breakdowns.add(new TargetUsageSummary(
                    target,
                    targetDuration.getOrDefault(target, 0L),
                    targetOpens.getOrDefault(target, 0),
                    targetSessions.getOrDefault(target, 0),
                    targetQuality.getOrDefault(target, UsageAggregator.Quality.COMPLETE)));
        }
        breakdowns.sort(Comparator.comparingLong((TargetUsageSummary s) -> s.totalMillis).reversed());

        // Process Habits
        int habitsCompleted = 0;
        int habitsTotal = 0;
        if (habitAggregates != null) {
            for (HabitDayAggregateEntity aggregate : habitAggregates) {
                if (aggregate.epochDay < startEpochDay || aggregate.epochDay > currentEpochDay) continue;
                habitsTotal++;
                if (aggregate.completed) habitsCompleted++;
            }
        }
        double habitConsistency = habitsTotal > 0 ? (double) habitsCompleted / habitsTotal : 0.0;

        // Process Focus
        int focusMinutes = 0;
        if (focusSessions != null) {
            for (FocusSessionEntity session : focusSessions) {
                if (session.startedAt >= startMillis && session.startedAt <= endMillis
                        && "COMPLETED".equals(session.state)) {
                    focusMinutes += (int) (session.accumulatedMillis / 60_000L);
                }
            }
        }

        boolean adequateSample = decisions >= 7;
        double followThroughRate = decisions > 0 ? (double) followThroughCount / decisions : 0.0;
        boolean hasData = decisions > 0 || totalUsageMillis > 0 || habitsCompleted > 0 || focusMinutes > 0;

        // Pattern summary
        PatternSummary patternSummary;
        if (adequateSample) {
            TimeWindow peakWindow = null;
            int maxWindowCount = -1;
            for (Map.Entry<TimeWindow, Integer> entry : windowCounts.entrySet()) {
                if (entry.getValue() > maxWindowCount) {
                    maxWindowCount = entry.getValue();
                    peakWindow = entry.getKey();
                }
            }

            String topTarget = null;
            int maxTargetCount = -1;
            for (Map.Entry<String, Integer> entry : targetCounts.entrySet()) {
                if (entry.getValue() > maxTargetCount) {
                    maxTargetCount = entry.getValue();
                    topTarget = entry.getKey();
                }
            }
            patternSummary = new PatternSummary(true, peakWindow, topTarget);
        } else {
            patternSummary = new PatternSummary(false, null, null);
        }

        return new Snapshot(
                hasData, range, followThroughRate, adequateSample, decisions,
                stepBackCount, continuedCount, rapidReopenCount, replacementChosenCount,
                focusStartedCount, followThroughCount, totalUsageMillis, totalOpens,
                totalSessions, overallQuality, breakdowns, habitsCompleted, habitsTotal,
                habitConsistency, focusMinutes, patternSummary);
    }
}
