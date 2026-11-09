package com.prottasha.guard.domain;

public final class GuardDecisionEngine {
    private GuardDecisionEngine() { }

    public static GuardDecision decide(
            GuardRule rule,
            GuardModels.GuardContext context,
            GuardModels.GuardUsage usage) {
        if (context.essentialTarget || !insideSchedule(
                context.minuteOfDay, rule.startMinute, rule.endMinute)) {
            return GuardDecision.ALLOW;
        }
        if (!context.serviceHealthy) return GuardDecision.UNAVAILABLE;
        if (usage.cooldownActive) return GuardDecision.ALLOW;
        switch (rule.mode) {
            case PAUSE:
                return GuardDecision.PAUSE;
            case BLOCK:
                return GuardDecision.BLOCK;
            case BUDGET:
                if (!usage.qualityAvailable) return GuardDecision.UNAVAILABLE;
                return usage.usedMinutes >= rule.budgetMinutes
                        ? GuardDecision.BLOCK : GuardDecision.GRACE;
            default:
                return GuardDecision.ALLOW;
        }
    }

    private static boolean insideSchedule(int minute, int start, int end) {
        if (start == end) return true;
        if (start < end) return minute >= start && minute < end;
        return minute >= start || minute < end;
    }
}
