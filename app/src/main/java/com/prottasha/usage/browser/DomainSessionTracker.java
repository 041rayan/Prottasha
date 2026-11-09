package com.prottasha.usage.browser;

public final class DomainSessionTracker {
    private String domain;
    private long startedAt;

    public void enter(String domain, long now) {
        this.domain = domain;
        startedAt = now;
    }

    public long leave(String domain, long now) {
        if (this.domain == null || !this.domain.equals(domain) || now <= startedAt) return 0L;
        long duration = now - startedAt;
        this.domain = null;
        startedAt = 0L;
        return duration;
    }
}
