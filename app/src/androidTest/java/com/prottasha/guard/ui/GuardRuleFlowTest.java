package com.prottasha.guard.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.core.widget.NestedScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.platform.LaunchableAppSource;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.ui.main.MainActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public final class GuardRuleFlowTest {
    private ProttashaApp application;
    private SessionUser user;

    @Before public void setUp() throws Exception {
        application = (ProttashaApp) ApplicationProvider.getApplicationContext();
        user = new SessionUser("guard-" + UUID.randomUUID(), "Guard", "guard@example.test", true);
        onDisk(() -> {
            application.appGraph().userSessionManager().switchTo(user);
            application.appGraph().userSessionDatabaseProvider().requireOpen()
                    .localAccountDao().updateOnboardingVersion(OnboardingViewModel.CURRENT_VERSION);
            return null;
        });
    }

    @After public void tearDown() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> application.appGraph().userSessionManager().signOut());
        onDisk(() -> { application.appGraph().userSessionDatabaseProvider().destroy(user); return null; });
    }

    @Test public void guardTabShowsHonestEmptyState() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.nav_guard)).perform(click());
            onView(withId(R.id.guard_health_card)).check(matches(isDisplayed()));
            onView(withId(R.id.guard_empty)).perform(scrollToInNested()).check(matches(isDisplayed()));
        }
    }

    @Test public void selectedAppStoresItsOwnMode() throws Exception {
        try (ActivityScenario<GuardRuleEditActivity> ignored = ActivityScenario.launch(
                GuardRuleEditActivity.createIntent(
                        ApplicationProvider.getApplicationContext(),
                        "com.example.video", "Video App"))) {
            onView(withId(R.id.guard_mode_block)).perform(click());
            onView(withId(R.id.guard_rule_save)).perform(scrollToInNested(), click());
        }
        waitForRule();
        List<GuardRuleEntity> rules = application.appGraph().userSessionDatabaseProvider().requireOpen()
                .guardRuleDao().list();
        assertFalse(rules.isEmpty());
        assertEquals("BLOCK", rules.get(0).mode);
        assertFalse(rules.get(0).isDomain);
    }

    @Test public void domainRuleIsStoredAndDifferentiatedFromAppRule() throws Exception {
        try (ActivityScenario<GuardRuleEditActivity> ignored = ActivityScenario.launch(
                GuardRuleEditActivity.createIntent(
                        ApplicationProvider.getApplicationContext(),
                        "youtube.com", "YouTube", true))) {
            onView(withId(R.id.guard_mode_pause)).perform(click());
            onView(withId(R.id.guard_rule_save)).perform(scrollToInNested(), click());
        }
        waitForRule();
        GuardRuleEntity rule = application.appGraph().userSessionDatabaseProvider().requireOpen()
                .guardRuleDao().findByTarget("youtube.com");
        assertNotNull(rule);
        assertTrue(rule.isDomain);
        assertEquals("PAUSE", rule.mode);
    }

    @Test public void disablingRuleRefreshesFilterWithoutRestart() throws Exception {
        GuardRuleEntity entity = new GuardRuleEntity(0L, "com.example.chat", "Chat App", "PAUSE", 0, 0, 0, true);
        long id = onDisk(() -> application.appGraph().userSessionDatabaseProvider().requireOpen()
                .guardRuleDao().upsert(entity));

        CountDownLatch latch = new CountDownLatch(1);
        application.appGraph().guardRuleRepository().setEnabled(id, false, result -> latch.countDown());
        assertTrue(latch.await(3, TimeUnit.SECONDS));

        GuardRuleEntity updated = onDisk(() -> application.appGraph().userSessionDatabaseProvider().requireOpen()
                .guardRuleDao().findByTarget("com.example.chat"));
        assertNotNull(updated);
        assertFalse(updated.enabled);
    }

    @Test public void editProtectionLocksRuleDuringActiveWindow() throws Exception {
        GuardRuleEntity protectedRule = new GuardRuleEntity(
                0L, "com.example.locked", "Locked App", "BLOCK", 0,
                0, 1440, true, 15, 5, 5, false, "", true, false);
        long ruleId = onDisk(() -> application.appGraph().userSessionDatabaseProvider().requireOpen()
                .guardRuleDao().upsert(protectedRule));
        protectedRule.id = ruleId;

        try (ActivityScenario<GuardRuleEditActivity> ignored = ActivityScenario.launch(
                GuardRuleEditActivity.editIntent(ApplicationProvider.getApplicationContext(), protectedRule))) {
            waitForBanner();
            onView(withId(R.id.guard_protection_banner)).check(matches(isDisplayed()));
            onView(withId(R.id.guard_rule_save)).check(matches(not(isEnabled())));
        }
    }

    @Test public void appPickerSourceNeverReturnsProttasha() {
        for (LaunchableAppSource.AppInfo app : new LaunchableAppSource(
                ApplicationProvider.getApplicationContext()).listNonEssential()) {
            assertFalse(app.packageName.startsWith("com.prottasha"));
        }
    }

    @Test public void appPickerFindsInstalledChromeOnBg7() {
        assertTrue(new LaunchableAppSource(ApplicationProvider.getApplicationContext())
                .listNonEssential().stream()
                .anyMatch(app -> "com.android.chrome".equals(app.packageName)));
    }

    private void waitForBanner() throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            try {
                onView(withId(R.id.guard_protection_banner)).check(matches(isDisplayed()));
                return;
            } catch (Throwable notYet) {
                Thread.sleep(50);
            }
        }
        onView(withId(R.id.guard_protection_banner)).check(matches(isDisplayed()));
    }

    private void waitForRule() throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            if (!application.appGraph().userSessionDatabaseProvider().requireOpen()
                    .guardRuleDao().list().isEmpty()) return;
            Thread.sleep(25);
        }
        throw new AssertionError("Guard rule was not saved");
    }

    private static ViewAction scrollToInNested() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "scroll to view in NestedScrollView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View parent = (View) view.getParent();
                while (parent != null && !(parent instanceof NestedScrollView)) {
                    Object p = parent.getParent();
                    parent = p instanceof View ? (View) p : null;
                }
                if (parent instanceof NestedScrollView) {
                    int[] viewLocation = new int[2];
                    int[] parentLocation = new int[2];
                    view.getLocationOnScreen(viewLocation);
                    parent.getLocationOnScreen(parentLocation);
                    int dy = viewLocation[1] - parentLocation[1];
                    ((NestedScrollView) parent).scrollBy(0, dy);
                }
                view.requestRectangleOnScreen(new Rect(0, 0, view.getWidth(), view.getHeight()), true);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        application.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
