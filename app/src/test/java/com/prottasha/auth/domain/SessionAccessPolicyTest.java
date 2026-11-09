package com.prottasha.auth.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SessionAccessPolicyTest {

    @Test
    public void rejectsEveryBackgroundActionForWrongOwner() {
        for (SessionAccessPolicy.Action action : SessionAccessPolicy.Action.values()) {
            assertFalse(SessionAccessPolicy.canRun(false, false, action));
            assertFalse(SessionAccessPolicy.canRun(false, true, action));
        }
    }

    @Test
    public void unlockedVerifiedOwnerCanRunAllDefinedActions() {
        for (SessionAccessPolicy.Action action : SessionAccessPolicy.Action.values()) {
            assertTrue(SessionAccessPolicy.canRun(true, false, action));
        }
    }

    @Test
    public void uiLockAllowsOnlyNeutralOrIdempotentBackgroundActions() {
        assertTrue(SessionAccessPolicy.canRun(
                true, true, SessionAccessPolicy.Action.RENDER_NEUTRAL_WIDGET));
        assertTrue(SessionAccessPolicy.canRun(
                true, true, SessionAccessPolicy.Action.COMPLETE_BUILD));
        assertTrue(SessionAccessPolicy.canRun(
                true, true, SessionAccessPolicy.Action.INCREMENT_AMOUNT));
        assertFalse(SessionAccessPolicy.canRun(
                true, true, SessionAccessPolicy.Action.CHECK_IN_AVOID));
        assertFalse(SessionAccessPolicy.canRun(
                true, true, SessionAccessPolicy.Action.OPEN_SENSITIVE_CONTENT));
    }
}
