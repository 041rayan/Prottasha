package com.prottasha.circle;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.circle.ui.AccountabilityActivity;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.ui.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.FutureTask;

@RunWith(AndroidJUnit4.class)
public final class CircleFlowTest {
    private ProttashaApp app;
    private SessionUser user;

    @Before public void setUp() throws Exception {
        app = (ProttashaApp) ApplicationProvider.getApplicationContext();
        user = new SessionUser("circle-" + UUID.randomUUID(), "Circle", "c@example.test", true);
        onDisk(() -> {
            app.appGraph().userSessionManager().switchTo(user);
            app.appGraph().userSessionDatabaseProvider().requireOpen().localAccountDao()
                    .updateOnboardingVersion(OnboardingViewModel.CURRENT_VERSION);
            return null;
        });
    }

    @After public void tearDown() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> app.appGraph().userSessionManager().signOut());
        onDisk(() -> { app.appGraph().userSessionDatabaseProvider().destroy(user); return null; });
    }

    @Test public void circleShowsClosedPodWithoutFabricatedMessages() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.nav_circle)).perform(click());
            onView(withText(R.string.circle_closed_status)).check(matches(isDisplayed()));
            onView(withId(R.id.circle_open_pod)).check(matches(isDisplayed()));
        }
    }

    @Test public void accountabilityUsesExplicitPickerAndSharesheetControls() {
        try (ActivityScenario<AccountabilityActivity> ignored =
                     ActivityScenario.launch(AccountabilityActivity.class)) {
            onView(withId(R.id.accountability_pick)).check(matches(isDisplayed()));
            onView(withId(R.id.accountability_share)).check(matches(isDisplayed()));
        }
    }

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        app.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
