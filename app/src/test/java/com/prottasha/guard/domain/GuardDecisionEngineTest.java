package com.prottasha.guard.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GuardDecisionEngineTest {

    @Test
    public void pauseBudgetAndBlockRemainIndependentModes() {
        GuardModels.GuardContext context = new GuardModels.GuardContext(12 * 60, true, false);
        assertEquals(GuardDecision.PAUSE, GuardDecisionEngine.decide(
                GuardRule.pause("com.example.video", 0, 0), context,
                new GuardModels.GuardUsage(0, true, false)));
        assertEquals(GuardDecision.GRACE, GuardDecisionEngine.decide(
                GuardRule.budget("com.example.video", 20, 0, 0), context,
                new GuardModels.GuardUsage(10, true, false)));
        assertEquals(GuardDecision.BLOCK, GuardDecisionEngine.decide(
                GuardRule.block("com.example.video", 0, 0), context,
                new GuardModels.GuardUsage(0, true, false)));
    }

    @Test
    public void budgetBlocksAtLimitAndFailsOpenWhenQualityIsUnavailable() {
        GuardRule rule = GuardRule.budget("com.example.video", 20, 0, 0);
        GuardModels.GuardContext context = new GuardModels.GuardContext(600, true, false);
        assertEquals(GuardDecision.BLOCK, GuardDecisionEngine.decide(
                rule, context, new GuardModels.GuardUsage(20, true, false)));
        assertEquals(GuardDecision.UNAVAILABLE, GuardDecisionEngine.decide(
                rule, context, new GuardModels.GuardUsage(0, false, false)));
    }

    @Test
    public void crossMidnightScheduleAppliesOnlyInsideWindow() {
        GuardRule rule = GuardRule.block("com.example.video", 22 * 60, 7 * 60);
        assertEquals(GuardDecision.BLOCK, GuardDecisionEngine.decide(
                rule, new GuardModels.GuardContext(23 * 60, true, false),
                new GuardModels.GuardUsage(0, true, false)));
        assertEquals(GuardDecision.ALLOW, GuardDecisionEngine.decide(
                rule, new GuardModels.GuardContext(12 * 60, true, false),
                new GuardModels.GuardUsage(0, true, false)));
    }

    @Test
    public void cooldownAllowsRecentlyCompletedPause() {
        GuardRule rule = GuardRule.pause("com.example.video", 0, 0);
        assertEquals(GuardDecision.ALLOW, GuardDecisionEngine.decide(
                rule, new GuardModels.GuardContext(600, true, false),
                new GuardModels.GuardUsage(0, true, true)));
    }

    @Test
    public void essentialPolicyProtectsEscapeAndSystemTargets() {
        EssentialTargetPolicy policy = new EssentialTargetPolicy("com.prottasha.debug");
        assertTrue(policy.isEssential("com.prottasha.debug"));
        assertTrue(policy.isEssential("com.android.settings"));
        assertTrue(policy.isEssential("com.android.dialer"));
        assertTrue(policy.isEssential("com.google.android.permissioncontroller"));
        assertFalse(policy.isEssential("com.example.video"));
    }
}
