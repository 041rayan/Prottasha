package com.prottasha.guard.domain;

public final class GuardRule {
    public enum Mode { PAUSE, BUDGET, BLOCK }

    public final String target;
    public final Mode mode;
    public final int budgetMinutes;
    public final int startMinute;
    public final int endMinute;

    private GuardRule(String target, Mode mode, int budgetMinutes, int startMinute, int endMinute) {
        this.target = target;
        this.mode = mode;
        this.budgetMinutes = budgetMinutes;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public static GuardRule pause(String target, int startMinute, int endMinute) {
        return new GuardRule(target, Mode.PAUSE, 0, startMinute, endMinute);
    }

    public static GuardRule budget(
            String target, int budgetMinutes, int startMinute, int endMinute) {
        return new GuardRule(target, Mode.BUDGET, Math.max(1, budgetMinutes),
                startMinute, endMinute);
    }

    public static GuardRule block(String target, int startMinute, int endMinute) {
        return new GuardRule(target, Mode.BLOCK, 0, startMinute, endMinute);
    }
}
