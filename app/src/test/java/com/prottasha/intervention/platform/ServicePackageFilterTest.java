package com.prottasha.intervention.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.domain.EssentialTargetPolicy;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public final class ServicePackageFilterTest {

    private ServicePackageFilter filter;
    private EssentialTargetPolicy essentialPolicy;

    @Before
    public void setUp() {
        filter = new ServicePackageFilter();
        essentialPolicy = new EssentialTargetPolicy("com.prottasha.app");
    }

    @Test
    public void replacePopulatesEnabledNonEssentialRules() {
        GuardRuleEntity appRule = new GuardRuleEntity(1L, "com.example.game", "Game", "PAUSE", 0, 0, 0, true);
        GuardRuleEntity domainRule = new GuardRuleEntity(2L, "reddit.com", "Reddit", "BLOCK", 0, 0, 0, true, 15, 5, 5, false, "", false, true);
        GuardRuleEntity disabledRule = new GuardRuleEntity(3L, "com.example.chat", "Chat", "PAUSE", 0, 0, 0, false);
        GuardRuleEntity essentialRule = new GuardRuleEntity(4L, "com.android.settings", "Settings", "BLOCK", 0, 0, 0, true);

        filter.replace(Arrays.asList(appRule, domainRule, disabledRule, essentialRule), essentialPolicy);

        assertTrue(filter.isMonitored("com.example.game"));
        assertTrue(filter.isMonitored("reddit.com"));
        assertFalse(filter.isMonitored("com.example.chat")); // disabled
        assertFalse(filter.isMonitored("com.android.settings")); // essential

        assertEquals("PAUSE", filter.modeFor("com.example.game"));
        assertEquals("BLOCK", filter.modeFor("reddit.com"));
        assertNull(filter.modeFor("com.example.chat"));

        assertNotNull(filter.ruleFor("reddit.com"));
        assertTrue(filter.ruleFor("reddit.com").isDomain);
    }

    @Test
    public void liveRefreshUpdatesMonitoredTargetsImmediately() {
        GuardRuleEntity rule1 = new GuardRuleEntity(1L, "com.example.video", "Video", "BUDGET", 30, 0, 0, true);
        filter.replace(Collections.singletonList(rule1), essentialPolicy);
        assertTrue(filter.isMonitored("com.example.video"));

        // Disable rule1
        GuardRuleEntity rule1Disabled = new GuardRuleEntity(1L, "com.example.video", "Video", "BUDGET", 30, 0, 0, false);
        filter.replace(Collections.singletonList(rule1Disabled), essentialPolicy);
        assertFalse(filter.isMonitored("com.example.video"));

        // Remove rule1 completely
        filter.replace(Collections.emptyList(), essentialPolicy);
        assertFalse(filter.isMonitored("com.example.video"));
    }
}
