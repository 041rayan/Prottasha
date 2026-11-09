package com.prottasha.intervention.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class InterventionStateMachineTest {
    @Test
    public void pauseStartsShownAndStepBackIsImmediateTerminal() {
        InterventionStateMachine machine = InterventionStateMachine.pause(false);
        assertEquals(InterventionStateMachine.State.SHOWN, machine.state());
        assertEquals(InterventionStateMachine.State.STEP_BACK, machine.stepBack());
        assertTrue(machine.isTerminal());
        assertEquals(InterventionStateMachine.State.STEP_BACK, machine.stepBack());
    }

    @Test
    public void requiredIntentionRejectsEmptyContinue() {
        InterventionStateMachine machine = InterventionStateMachine.pause(true);
        assertThrows(IllegalArgumentException.class, () -> machine.continueWith("  "));
        assertFalse(machine.isTerminal());
        assertEquals(InterventionStateMachine.State.CONTINUE,
                machine.continueWith("Open only the saved message"));
    }

    @Test
    public void blockHasNoContinuePath() {
        InterventionStateMachine machine = InterventionStateMachine.block();
        assertEquals(InterventionStateMachine.State.BLOCKED, machine.state());
        assertTrue(machine.isTerminal());
        assertThrows(IllegalStateException.class, () -> machine.continueWith("anything"));
    }

    @Test
    public void rapidReopenIsDistinctFromDurableStepBack() {
        assertEquals(InterventionStateMachine.State.RAPID_REOPEN,
                InterventionStateMachine.classifyReturn(1000L, 5000L, 10_000L));
        assertEquals(InterventionStateMachine.State.FOLLOW_THROUGH,
                InterventionStateMachine.classifyReturn(1000L, 20_000L, 10_000L));
    }
}
