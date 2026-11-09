package com.prottasha.guard.domain;

public final class BudgetCalculator {
    private BudgetCalculator() { }
    public static int remaining(int budgetMinutes, int usedMinutes) {
        return Math.max(0, budgetMinutes - Math.max(0, usedMinutes));
    }
}
