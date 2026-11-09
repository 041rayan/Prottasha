package com.prottasha.intervention;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prottasha.R;
import com.prottasha.intervention.ui.AccessibilityDisclosureActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class AccessibilityDisclosureTest {
    @Test
    public void disclosureRequiresAffirmativeConsentAndStoresVersion() {
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("accessibility_consent", Context.MODE_PRIVATE)
                .edit().clear().commit();
        try (ActivityScenario<AccessibilityDisclosureActivity> ignored =
                     ActivityScenario.launch(AccessibilityDisclosureActivity.class)) {
            onView(withId(R.id.accessibility_continue)).check(matches(not(isEnabled())));
            onView(withId(R.id.accessibility_consent)).perform(click());
            onView(withId(R.id.accessibility_continue)).check(matches(isEnabled()));
            onView(withId(R.id.accessibility_continue)).perform(click());
        }
        assertTrue(AccessibilityDisclosureActivity.hasCurrentConsent(context));
    }
}
