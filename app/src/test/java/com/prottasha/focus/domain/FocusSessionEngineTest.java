package com.prottasha.focus.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class FocusSessionEngineTest {
    @Test
    public void pauseResumeAndProcessTimeProduceDeterministicRemainingTime() {
        FocusSessionEngine engine = FocusSessionEngine.start(25 * 60_000L, 1000L);
        assertEquals(24 * 60_000L, engine.remainingAt(61_000L));
        engine.pause(61_000L);
        assertEquals(24 * 60_000L, engine.remainingAt(121_000L));
        engine.resume(121_000L);
        assertEquals(23 * 60_000L, engine.remainingAt(181_000L));
    }

    @Test
    public void endIsIdempotentAndCompletionOnlyOccursAtZero() {
        FocusSessionEngine engine = FocusSessionEngine.start(1000L, 0L);
        assertFalse(engine.isCompleteAt(999L));
        assertTrue(engine.isCompleteAt(1000L));
        engine.end();
        engine.end();
        assertTrue(engine.isEnded());
    }
}
