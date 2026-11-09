package com.prottasha.onboarding;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.ui.main.MainActivity;

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
            onView(withId(R.id.onboarding_finish_button)).perform(click());
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
    public void shellHasFiveTabsHonestTodayAndPredictableBack() {
        application.appGraph().userSessionDatabaseProvider().requireOpen()
                .localAccountDao().updateOnboardingVersion(OnboardingViewModel.CURRENT_VERSION);
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
            onView(withId(R.id.shell_placeholder_title)).check(matches(withText("Guard")));
            pressBack();
            onView(withId(R.id.today_empty_title)).check(matches(isDisplayed()));
        }
    }

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        application.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
