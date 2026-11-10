package com.prottasha.settings.security;

public final class AppLockManager {
    private final long timeoutMillis;
    private long backgroundAt = -1L;
    private boolean forced;

    public AppLockManager(long timeoutMillis) {
        this.timeoutMillis = Math.max(0L, timeoutMillis);
    }

    public void onBackground(long now) { backgroundAt = now; }
    public void unlock() { backgroundAt = -1L; forced = false; }
    public void onAccountChanged() { forced = true; }

    public boolean requiresUnlock(long now) {
        return forced || (backgroundAt >= 0 && now - backgroundAt > timeoutMillis);
    }
}
