package com.prottasha.habits.domain;

public final class HabitCompletionEngine {

    private HabitCompletionEngine() {
    }

    public static HabitModels.CompletionResult apply(
            HabitModels.Type type,
            int target,
            int current,
            HabitAction action) {
        int safeTarget = Math.max(1, target);
        if (action.kind == HabitAction.Kind.LAPSE) {
            return new HabitModels.CompletionResult(current, false, true);
        }
        if (action.kind == HabitAction.Kind.UNDO) {
            return new HabitModels.CompletionResult(Math.max(0, current - 1), false, false);
        }
        if (type == HabitModels.Type.AMOUNT) {
            int value = Math.min(safeTarget, current + action.amount);
            return new HabitModels.CompletionResult(value, value >= safeTarget, false);
        }
        return new HabitModels.CompletionResult(1, true, false);
    }
}
