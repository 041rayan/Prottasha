package com.prottasha.auth;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertThrows;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.prottasha.R;
import com.prottasha.auth.ui.AuthActivity;
import com.prottasha.auth.ui.LaunchActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class AuthFlowTest {

    @Before
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @After
    public void closeStrayActivity() {
        try {
            pressBack();
        } catch (RuntimeException ignored) {
            // A test may already have destroyed the Activity under test.
        }
    }

    @Test
    public void signedOutLauncherRoutesToSignIn() {
        try (ActivityScenario<LaunchActivity> ignored =
                     ActivityScenario.launch(LaunchActivity.class)) {
            onView(withId(R.id.auth_sign_in_title)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void backFromAuthenticationCannotRevealMain() {
        try (ActivityScenario<AuthActivity> scenario =
                     ActivityScenario.launch(AuthActivity.class)) {
            assertThrows(NoActivityResumedException.class, () -> pressBack());
        }
    }

    @Test
    public void createAccountShowsInlinePasswordMismatchWithoutSubmitting() {
        try (ActivityScenario<AuthActivity> ignored =
                     ActivityScenario.launch(AuthActivity.class)) {
            onView(withId(R.id.auth_create_link)).perform(click());
            onView(withId(R.id.auth_display_name_input)).perform(replaceText("Alice"));
            onView(withId(R.id.auth_email_input)).perform(replaceText("alice@example.test"));
            onView(withId(R.id.auth_password_input)).perform(replaceText("Strong123"));
            onView(withId(R.id.auth_confirm_password_input)).perform(replaceText("Strong124"));
            onView(withId(R.id.auth_terms_checkbox)).perform(click());
            onView(withId(R.id.auth_create_button)).perform(click());

            onView(withId(R.id.auth_error_summary))
                    .check(matches(withText(R.string.auth_error_password_mismatch)))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void signInEmailSurvivesActivityRecreation() {
        try (ActivityScenario<AuthActivity> scenario =
                     ActivityScenario.launch(AuthActivity.class)) {
            onView(withId(R.id.auth_email_input))
                    .perform(replaceText("alice@example.test"), ViewActions.closeSoftKeyboard());

            scenario.recreate();

            onView(withId(R.id.auth_email_input))
                    .check(matches(withText("alice@example.test")));
            onView(isRoot()).check(matches(isDisplayed()));
        }
    }
}
