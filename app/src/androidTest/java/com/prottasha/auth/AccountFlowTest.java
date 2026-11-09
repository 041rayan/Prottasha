package com.prottasha.auth;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prottasha.R;
import com.prottasha.settings.account.AccountActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class AccountFlowTest {

    @Test
    public void signedOutAccountScreenCannotStartDeletion() {
        try (ActivityScenario<AccountActivity> scenario =
                     ActivityScenario.launch(AccountActivity.class)) {
            scenario.onActivity(activity -> {
                assertFalse(activity.findViewById(R.id.account_delete_button).isEnabled());
                android.widget.TextView status = activity.findViewById(R.id.account_status);
                assertEquals(activity.getString(R.string.account_no_verified_session),
                        status.getText().toString());
            });
        }
    }

    @Test
    public void passwordIsNotRestoredAcrossRecreation() {
        try (ActivityScenario<AccountActivity> scenario =
                     ActivityScenario.launch(AccountActivity.class)) {
            onView(withId(R.id.account_password_input)).perform(replaceText("DoNotPersist123"));

            scenario.recreate();

            onView(withId(R.id.account_password_input)).check(matches(withText("")));
        }
    }
}
