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
                assertFalse(activity.findViewById(R.id.account_edit_profile_button).isEnabled());
                assertFalse(activity.findViewById(R.id.account_change_password_button).isEnabled());
                assertFalse(activity.findViewById(R.id.account_change_email_button).isEnabled());
                assertFalse(activity.findViewById(R.id.account_sign_out_button).isEnabled());
                assertFalse(activity.findViewById(R.id.account_switch_button).isEnabled());
                assertFalse(activity.findViewById(R.id.account_remove_local_button).isEnabled());
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
            scenario.onActivity(activity -> {
                android.widget.EditText pass = activity.findViewById(R.id.account_password_input);
                pass.setText("DoNotPersist123");
                android.widget.EditText reauth = activity.findViewById(R.id.account_reauth_password_input);
                reauth.setText("Reauth123");
                android.widget.EditText newPass = activity.findViewById(R.id.account_new_password_input);
                newPass.setText("NewPass123");
            });

            scenario.recreate();

            scenario.onActivity(activity -> {
                android.widget.EditText pass = activity.findViewById(R.id.account_password_input);
                assertEquals("", pass.getText().toString());
                android.widget.EditText reauth = activity.findViewById(R.id.account_reauth_password_input);
                assertEquals("", reauth.getText().toString());
                android.widget.EditText newPass = activity.findViewById(R.id.account_new_password_input);
                assertEquals("", newPass.getText().toString());
            });
        }
    }
}
