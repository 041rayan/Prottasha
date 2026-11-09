package com.prottasha.core.time;

import java.time.Instant;

public interface ClockProvider {
    Instant now();
}
