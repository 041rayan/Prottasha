package com.prottasha.habits.domain;

public final class HabitAction {

    public enum Kind { COMPLETE, LAPSE, INCREMENT, UNDO }

    public final Kind kind;
    public final String nonce;
    public final int amount;
    public final long revertedEventId;
    public final long habitId;
    public final long epochDay;

    private HabitAction(Kind kind, String nonce, int amount, long revertedEventId,
            long habitId, long epochDay) {
        if (nonce == null || nonce.trim().isEmpty()) {
            throw new IllegalArgumentException("nonce is required");
        }
        this.kind = kind;
        this.nonce = nonce;
        this.amount = amount;
        this.revertedEventId = revertedEventId;
        this.habitId = habitId;
        this.epochDay = epochDay;
    }

    public static HabitAction complete(String nonce) {
        return new HabitAction(Kind.COMPLETE, nonce, 1, 0L, 0L, 0L);
    }

    public static HabitAction lapse(String nonce) {
        return new HabitAction(Kind.LAPSE, nonce, 0, 0L, 0L, 0L);
    }

    public static HabitAction increment(String nonce, int amount) {
        return new HabitAction(Kind.INCREMENT, nonce, Math.max(1, amount), 0L, 0L, 0L);
    }

    public static HabitAction undo(String nonce, long eventId) {
        return new HabitAction(Kind.UNDO, nonce, 0, eventId, 0L, 0L);
    }

    public HabitAction forHabit(long habitId, long epochDay) {
        return new HabitAction(kind, nonce, amount, revertedEventId, habitId, epochDay);
    }
}
