package com.prottasha.core.result;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class AppResultTest {

    @Test
    public void successContainsOnlyItsValue() {
        AppResult<String> result = AppResult.success("ready");

        assertTrue(result.isSuccess());
        assertEquals("ready", result.valueOrNull());
        assertNull(result.errorOrNull());
    }

    @Test
    public void failureContainsOnlyItsCause() {
        IllegalStateException cause = new IllegalStateException("offline");
        AppResult<String> result = AppResult.failure(cause);

        assertFalse(result.isSuccess());
        assertNull(result.valueOrNull());
        assertSame(cause, result.errorOrNull());
    }

    @Test(expected = NullPointerException.class)
    public void successRejectsNullValue() {
        AppResult.success(null);
    }

    @Test(expected = NullPointerException.class)
    public void failureRejectsNullCause() {
        AppResult.failure(null);
    }
}
