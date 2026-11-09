package com.prottasha.intervention.domain;

public final class InterventionStateMachine {
    public enum State {
        SHOWN,
        STEP_BACK,
        RAPID_REOPEN,
        FOLLOW_THROUGH,
        CONTINUE,
        BLOCKED
    }

    private final boolean requireIntention;
    private State state;

    private InterventionStateMachine(State initial, boolean requireIntention) {
        state = initial;
        this.requireIntention = requireIntention;
    }

    public static InterventionStateMachine pause(boolean requireIntention) {
        return new InterventionStateMachine(State.SHOWN, requireIntention);
    }

    public static InterventionStateMachine block() {
        return new InterventionStateMachine(State.BLOCKED, false);
    }

    public State state() { return state; }

    public State stepBack() {
        if (!isTerminal()) state = State.STEP_BACK;
        return state;
    }

    public State continueWith(String intention) {
        if (state == State.BLOCKED) throw new IllegalStateException("Block has no continue path");
        if (isTerminal()) return state;
        if (requireIntention && (intention == null || intention.trim().isEmpty())) {
            throw new IllegalArgumentException("Intention is required");
        }
        state = State.CONTINUE;
        return state;
    }

    public boolean isTerminal() {
        return state == State.STEP_BACK || state == State.CONTINUE || state == State.BLOCKED;
    }

    public static State classifyReturn(long stepBackAt, long reopenedAt, long rapidWindowMillis) {
        return reopenedAt - stepBackAt <= rapidWindowMillis
                ? State.RAPID_REOPEN : State.FOLLOW_THROUGH;
    }
}
