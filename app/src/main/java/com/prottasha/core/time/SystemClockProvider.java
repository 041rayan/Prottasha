package com.prottasha.core.time;

import java.time.Instant;

public final class SystemClockProvider implements ClockProvider {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
