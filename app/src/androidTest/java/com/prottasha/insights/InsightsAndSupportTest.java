package com.prottasha.insights;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.graphics.Rect;
import android.view.View;
import androidx.core.widget.NestedScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.hamcrest.Matcher;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.auth.ui.AuthActivity;
import com.prottasha.insights.ui.ReflectionActivity;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.ui.crisis.CrisisHelpActivity;
import com.prottasha.ui.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.FutureTask;

@RunWith(AndroidJUnit4.class)
public final class InsightsAndSupportTest {
    private ProttashaApp app;
    private SessionUser user;
    private ActivityScenario<AuthActivity> foregroundHost;

    @Before public void setUp() throws Exception {
        app = (ProttashaApp) ApplicationProvider.getApplicationContext();
        foregroundHost = ActivityScenario.launch(AuthActivity.class);
        user = new SessionUser("insight-" + UUID.randomUUID(), "Insight", "i@example.test", true);
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
        foregroundHost.close();
    }

    @Test public void insightsShowHonestNoDataAndReflectionPersists() throws Exception {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.nav_insights)).perform(click());
            onView(withText(R.string.insights_no_data)).check(matches(isDisplayed()));
        }
        try (ActivityScenario<ReflectionActivity> ignored =
                     ActivityScenario.launch(ReflectionActivity.class)) {
            onView(withId(R.id.reflection_fact)).perform(replaceText("Opened the app twice"));
            onView(withId(R.id.reflection_interpretation)).perform(replaceText("I was tired"));
            onView(withId(R.id.reflection_lesson)).perform(replaceText("Rest before deciding"));
            onView(withId(R.id.reflection_save)).perform(click());
        }
        waitForReflection();
        assertEquals("Opened the app twice", app.appGraph().userSessionDatabaseProvider()
                .requireOpen().insightsDao().recent(1).get(0).fact);
    }

    @Test public void triggerPlanAndSuccessMemoryPersist() throws Exception {
        onDisk(() -> {
            app.appGraph().triggerRepository().save("Late night scrolling", "Put phone away and read a book");
            app.appGraph().identityRepository().saveSuccessMemory("Took 3 deep breaths and stepped back");
            return null;
        });

        assertEquals(1, onDisk(() -> app.appGraph().triggerRepository().list().size()).intValue());
        assertEquals("Late night scrolling", onDisk(() -> app.appGraph().triggerRepository().list().get(0).cue));
        assertEquals(1, onDisk(() -> app.appGraph().identityRepository().listSuccessMemories().size()).intValue());
        assertEquals("Took 3 deep breaths and stepped back",
                onDisk(() -> app.appGraph().identityRepository().listSuccessMemories().get(0).text));
    }

    @Test public void rehearsalSessionPersistsInRehearsalActivity() throws Exception {
        try (ActivityScenario<com.prottasha.insights.ui.RehearsalActivity> ignored =
                     ActivityScenario.launch(com.prottasha.insights.ui.RehearsalActivity.class)) {
            onView(withId(R.id.rehearsal_complete_button)).perform(scrollToInNested(), click());
        }
        waitForRehearsal();
        assertEquals(1, onDisk(() -> app.appGraph().insightsRepository().rehearsalCount()).intValue());
    }

    @Test public void supportShowsSourcedDialableEntry() {
        try (ActivityScenario<CrisisHelpActivity> ignored =
                     ActivityScenario.launch(CrisisHelpActivity.class)) {
            onView(withText(org.hamcrest.Matchers.containsString("999")))
                    .check(matches(isDisplayed()));
            onView(withText(org.hamcrest.Matchers.containsString("VERIFIED")))
                    .check(matches(isDisplayed()));
        }
    }

    private void waitForReflection() throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            if (!app.appGraph().userSessionDatabaseProvider().requireOpen()
                    .insightsDao().recent(1).isEmpty()) return;
            Thread.sleep(25);
        }
        throw new AssertionError("Reflection was not saved");
    }

    private void waitForRehearsal() throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            if (app.appGraph().userSessionDatabaseProvider().requireOpen()
                    .rehearsalDao().count() > 0) return;
            Thread.sleep(25);
        }
        throw new AssertionError("Rehearsal was not saved");
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
        app.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
