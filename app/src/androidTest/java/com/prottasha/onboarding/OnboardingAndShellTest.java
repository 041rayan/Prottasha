package com.prottasha.onboarding;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.identity.data.SuccessMemoryEntity;
import com.prottasha.intervention.data.InterventionEventEntity;
import com.prottasha.ui.main.MainActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.FutureTask;

@RunWith(AndroidJUnit4.class)
public final class OnboardingAndShellTest {

    private ProttashaApp application;
    private SessionUser user;

    @Before
    public void openVerifiedEncryptedSession() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        application = (ProttashaApp) context;
        String token = UUID.randomUUID().toString();
        user = new SessionUser(
                "shell-" + token, "Scholar", "shell@example.test", true);
        onDisk(() -> {
            application.appGraph().userSessionManager().switchTo(user);
            return null;
        });
    }

    @After
    public void destroySession() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> application.appGraph().userSessionManager().signOut());
        onDisk(() -> {
            application.appGraph().userSessionDatabaseProvider().destroy(user);
            return null;
        });
    }

    @Test
    public void skipCompletesVersionWithoutInventingIdentity() throws Exception {
        try (ActivityScenario<OnboardingActivity> ignored =
                     ActivityScenario.launch(OnboardingActivity.class)) {
            onView(withId(R.id.onboarding_skip_button)).perform(click());
            onView(withId(R.id.main_bottom_navigation)).check(matches(isDisplayed()));
        }

        UserDatabase database = application.appGraph().userSessionDatabaseProvider().requireOpen();
        assertEquals(OnboardingViewModel.CURRENT_VERSION,
                database.localAccountDao().requireAccount().onboardingVersion);
        assertNull(database.identityDao().findActive());
    }

    @Test
    public void purposeAndIdentityPersistOnlyWhenFinished() throws Exception {
        try (ActivityScenario<OnboardingActivity> ignored =
                     ActivityScenario.launch(OnboardingActivity.class)) {
            onView(withId(R.id.onboarding_purpose_input)).perform(replaceText("Protect my attention"));
            onView(withId(R.id.onboarding_continue_button)).perform(click());
            onView(withId(R.id.onboarding_identity_input)).perform(
                    replaceText("I am a focused scholar"));
            onView(withId(R.id.onboarding_finish_button)).perform(scrollToInNested(), click());
            onView(withId(R.id.main_bottom_navigation)).check(matches(isDisplayed()));
        }

        IdentityEntity identity = application.appGraph()
                .userSessionDatabaseProvider().requireOpen().identityDao().findActive();
        assertEquals("I am a focused scholar", identity.statement);
        assertEquals("Protect my attention", identity.purpose);
    }

    @Test
    public void purposeDraftSurvivesActivityRecreationWithoutDatabaseWrite() {
        try (ActivityScenario<OnboardingActivity> scenario =
                     ActivityScenario.launch(OnboardingActivity.class)) {
            onView(withId(R.id.onboarding_purpose_input)).perform(
                    replaceText("Keep this draft"));

            scenario.recreate();

            onView(withId(R.id.onboarding_purpose_input))
                    .check(matches(withText("Keep this draft")));
            assertNull(application.appGraph()
                    .userSessionDatabaseProvider().requireOpen().identityDao().findActive());
        }
    }

    @Test
    public void allSevenStagesFlowPersistsIdentitySuccessMemoryAndVersion() throws Exception {
        try (ActivityScenario<OnboardingActivity> ignored =
                     ActivityScenario.launch(OnboardingActivity.class)) {
            // Stage 1: Purpose
            onView(withId(R.id.onboarding_purpose_input)).perform(replaceText("Deep academic focus"));
            onView(withId(R.id.onboarding_continue_button)).perform(click());

            // Stage 2: Identity & Success memory
            onView(withId(R.id.onboarding_identity_input)).perform(replaceText("I am a disciplined researcher"));
            onView(withId(R.id.onboarding_success_memory_input)).perform(replaceText("Defended proposal with honors"));
            onView(withId(R.id.onboarding_continue_button)).perform(scrollToInNested(), click());

            // Stage 3: Targets selection
            onView(withId(R.id.onboarding_targets_continue_button)).perform(scrollToInNested(), click());

            // Stage 4: Rule mode selection
            onView(withId(R.id.onboarding_mode_pause_radio)).perform(scrollToInNested(), click());
            onView(withId(R.id.onboarding_rules_continue_button)).perform(scrollToInNested(), click());

            // Stage 5: Disclosure
            onView(withId(R.id.onboarding_disclosure_continue_button)).perform(scrollToInNested(), click());

            // Stage 6: Permissions
            onView(withId(R.id.onboarding_permissions_continue_button)).perform(scrollToInNested(), click());

            // Stage 7: Intervention preview
            onView(withId(R.id.onboarding_preview_finish_button)).perform(scrollToInNested(), click());

            // Lands on Shell
            onView(withId(R.id.main_bottom_navigation)).check(matches(isDisplayed()));
        }

        UserDatabase db = application.appGraph().userSessionDatabaseProvider().requireOpen();
        assertEquals(OnboardingViewModel.CURRENT_VERSION,
                db.localAccountDao().requireAccount().onboardingVersion);

        IdentityEntity activeIdentity = db.identityDao().findActive();
        assertNotNull(activeIdentity);
        assertEquals("I am a disciplined researcher", activeIdentity.statement);
        assertEquals("Deep academic focus", activeIdentity.purpose);

        SuccessMemoryEntity memory = db.identityDao().findLatestSuccessMemory();
        assertNotNull(memory);
        assertEquals("Defended proposal with honors", memory.text);
    }

    @Test
    public void versionUpgradeRoutesV1DirectlyToTargets() throws Exception {
        onDisk(() -> {
            application.appGraph().userSessionDatabaseProvider().requireOpen()
                    .localAccountDao().updateOnboardingVersion(1);
            return null;
        });
        try (ActivityScenario<OnboardingActivity> ignored =
                     ActivityScenario.launch(OnboardingActivity.class)) {
            onView(withId(R.id.onboarding_targets_title)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void shellHasFiveTabsHonestTodayAndPredictableBack() throws Exception {
        onDisk(() -> {
            application.appGraph().userSessionDatabaseProvider().requireOpen()
                    .localAccountDao().updateOnboardingVersion(OnboardingViewModel.CURRENT_VERSION);
            return null;
        });
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                com.google.android.material.bottomnavigation.BottomNavigationView navigation =
                        activity.findViewById(R.id.main_bottom_navigation);
                assertEquals(5, navigation.getMenu().size());
            });
            onView(withId(R.id.today_empty_title)).check(matches(isDisplayed()));
            onView(withId(R.id.main_lifeline_button)).check(matches(isDisplayed()));
            onView(withId(R.id.main_settings_button)).check(matches(isDisplayed()));

            onView(withId(R.id.nav_guard)).perform(click());
            onView(withId(R.id.guard_add_button)).check(matches(isDisplayed()));
            pressBack();
            onView(withId(R.id.today_empty_title)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void todayScreenRendersIdentitySuccessMemoryAndTruthfulHopeHorizon() throws Exception {
        UserDatabase db = application.appGraph().userSessionDatabaseProvider().requireOpen();
        onDisk(() -> {
            db.localAccountDao().updateOnboardingVersion(OnboardingViewModel.CURRENT_VERSION);
            application.appGraph().identityRepository().saveActive(
                    "I am a focused researcher", "Protect depth", "Wrote key thesis chapter");
            db.guardRuleDao().upsert(new GuardRuleEntity(
                    0L, "com.example.distraction", "Distraction", "PAUSE", 0, 0, 0, true));
            db.interventionDao().insert(new InterventionEventEntity(
                    0L, "com.example.distraction", "STEP_BACK", System.currentTimeMillis(), ""));
            return null;
        });

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForView(withId(R.id.today_identity_card), 5000);
            onView(withId(R.id.today_identity_statement))
                    .check(matches(withText("I am a focused researcher")));
            onView(withId(R.id.today_identity_purpose))
                    .check(matches(withText("Protect depth")));
            onView(withId(R.id.today_success_memory))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.today_boundaries_count))
                    .check(matches(withText("1 app boundaries guarded")));
            waitForView(withId(R.id.today_hope_horizon_content), 5000);
            onView(withId(R.id.today_hope_horizon_summary))
                    .check(matches(withText("100% mindful exits")));
        }
    }

    private static void waitForView(Matcher<View> matcher, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                onView(matcher).check(matches(isDisplayed()));
                return;
            } catch (Throwable notYet) {
                android.os.SystemClock.sleep(50);
            }
        }
        onView(matcher).check(matches(isDisplayed()));
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
