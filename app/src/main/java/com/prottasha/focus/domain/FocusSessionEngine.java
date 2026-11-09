package com.prottasha.focus.domain;

public final class FocusSessionEngine {
    private final long durationMillis;
    private long startedAt;
    private long accumulatedMillis;
    private boolean paused;
    private boolean ended;

    private FocusSessionEngine(long durationMillis, long startedAt) {
        this.durationMillis = Math.max(1L, durationMillis);
        this.startedAt = startedAt;
    }

    public static FocusSessionEngine start(long durationMillis, long now) {
        return new FocusSessionEngine(durationMillis, now);
    }

    public long remainingAt(long now) {
        long elapsed = accumulatedMillis + (paused || ended ? 0 : Math.max(0, now - startedAt));
        return Math.max(0, durationMillis - elapsed);
    }

    public void pause(long now) {
        if (!paused && !ended) {
            accumulatedMillis += Math.max(0, now - startedAt);
            paused = true;
        }
    }

    public void resume(long now) {
        if (paused && !ended) {
            startedAt = now;
            paused = false;
        }
    }

    public boolean isCompleteAt(long now) { return remainingAt(now) == 0; }
    public void end() { ended = true; }
    public boolean isEnded() { return ended; }
    public boolean isPaused() { return paused; }
}
