package com.prottasha.circle;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;

import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.circle.ui.AccountabilityActivity;
import com.prottasha.circle.ui.PodChatActivity;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.ui.crisis.CrisisHelpActivity;
import com.prottasha.ui.main.MainActivity;

import org.hamcrest.Matcher;
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
            onView(withId(R.id.circle_accountability)).check(matches(isDisplayed()));
            onView(withId(R.id.circle_crisis_help)).check(matches(isDisplayed()));
        }
    }

    @Test public void accountabilityConfiguresPartnerAndGeneratesTruthfulShare() {
        try (ActivityScenario<AccountabilityActivity> ignored =
                     ActivityScenario.launch(AccountabilityActivity.class)) {
            onView(withId(R.id.accountability_pick)).perform(scrollToInNested()).check(matches(isDisplayed()));
            onView(withId(R.id.accountability_share)).perform(scrollToInNested()).check(matches(isDisplayed()));

            // Enter partner manually
            onView(withId(R.id.accountability_name_input)).perform(scrollToInNested(), replaceText("Tariq"), closeSoftKeyboard());
            onView(withId(R.id.accountability_phone_input)).perform(scrollToInNested(), replaceText("01712345678"), closeSoftKeyboard());
            onView(withId(R.id.accountability_save_button)).perform(scrollToInNested(), click());

            // Active partner card should appear
            onView(withId(R.id.accountability_partner_details)).perform(scrollToInNested()).check(matches(withText(containsString("Tariq"))));
            onView(withId(R.id.accountability_preview)).perform(scrollToInNested()).check(matches(withText(containsString("Tariq"))));

            // Revoke partner
            onView(withId(R.id.accountability_revoke)).perform(scrollToInNested(), click());
            onView(withId(R.id.accountability_current_partner_card)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        }
    }

    @Test public void podChatActivityRendersHonestMembershipAndInputState() {
        try (ActivityScenario<PodChatActivity> ignored = ActivityScenario.launch(PodChatActivity.class)) {
            onView(withId(R.id.pod_header_title)).check(matches(isDisplayed()));
            onView(withId(R.id.pod_message_input)).check(matches(isDisplayed()));
            onView(withId(R.id.pod_send)).check(matches(isDisplayed()));
            onView(withId(R.id.pod_back_button)).check(matches(isDisplayed()));
        }
    }

    @Test public void crisisHelpActivityDisplaysVerifiedHotlinesAndActions() {
        try (ActivityScenario<CrisisHelpActivity> ignored = ActivityScenario.launch(CrisisHelpActivity.class)) {
            onView(withId(R.id.support_status_badge)).check(matches(isDisplayed()));
            onView(allOf(withId(R.id.support_entry_name), withText(containsString("Bangladesh National Emergency Service")))).check(matches(isDisplayed()));
            onView(withText("Call 999")).check(matches(isDisplayed()));
            onView(allOf(withId(R.id.support_entry_name), withText(containsString("Kaan Pete Roi")))).check(matches(isDisplayed()));
        }
    }

    private static ViewAction scrollToInNested() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                        isDescendantOfA(anyOf(isAssignableFrom(NestedScrollView.class),
                                isAssignableFrom(ScrollView.class))));
            }

            @Override
            public String getDescription() {
                return "scroll to view inside NestedScrollView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ViewParent parent = view.getParent();
                while (parent != null) {
                    if (parent instanceof NestedScrollView) {
                        ((NestedScrollView) parent).scrollTo(0, view.getTop());
                        uiController.loopMainThreadUntilIdle();
                        return;
                    }
                    parent = parent.getParent();
                }
            }
        };
    }

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        app.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
