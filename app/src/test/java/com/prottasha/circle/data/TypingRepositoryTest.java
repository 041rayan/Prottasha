package com.prottasha.circle.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class TypingRepositoryTest {

    @Test
    public void constantsMatchSpec() {
        assertEquals(1500L, TypingRepository.TYPING_DEBOUNCE_MILLIS);
        assertEquals(8000L, TypingRepository.STALE_TYPING_THRESHOLD_MILLIS);
    }

    @Test
    public void isTypingActiveExcludesStaleTimestamps() {
        long now = 100000L;
        assertTrue(TypingRepository.isTypingActive(true, now - 2000L, now));
        assertFalse(TypingRepository.isTypingActive(true, now - 8001L, now));
        assertFalse(TypingRepository.isTypingActive(false, now - 1000L, now));
    }
}
