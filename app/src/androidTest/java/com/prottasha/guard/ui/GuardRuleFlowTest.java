package com.prottasha.guard.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.guard.platform.LaunchableAppSource;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.ui.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.FutureTask;

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
            onView(withText(R.string.guard_empty)).check(matches(isDisplayed()));
        }
    }

    @Test public void selectedAppStoresItsOwnMode() throws Exception {
        try (ActivityScenario<GuardRuleEditActivity> ignored = ActivityScenario.launch(
                GuardRuleEditActivity.createIntent(
                        ApplicationProvider.getApplicationContext(),
                        "com.example.video", "Video App"))) {
            onView(withId(R.id.guard_mode_block)).perform(click());
            onView(withId(R.id.guard_rule_save)).perform(click());
        }
        waitForRule();
        assertFalse(application.appGraph().userSessionDatabaseProvider().requireOpen()
                .guardRuleDao().list().isEmpty());
        org.junit.Assert.assertEquals("BLOCK", application.appGraph()
                .userSessionDatabaseProvider().requireOpen().guardRuleDao().list().get(0).mode);
    }

    @Test public void appPickerSourceNeverReturnsProttasha() {
        for (LaunchableAppSource.AppInfo app : new LaunchableAppSource(
                ApplicationProvider.getApplicationContext()).listNonEssential()) {
            assertFalse(app.packageName.startsWith("com.prottasha"));
        }
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

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        application.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
