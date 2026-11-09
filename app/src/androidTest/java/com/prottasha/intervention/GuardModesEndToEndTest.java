package com.prottasha.intervention;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prottasha.R;
import com.prottasha.ui.pause.PauseInterstitialActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class GuardModesEndToEndTest {
    @Test
    public void pauseOffersStepBackAndIntentionalContinue() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                PauseInterstitialActivity.class)
                .putExtra(PauseInterstitialActivity.EXTRA_TARGET_PACKAGE, "Video App")
                .putExtra("guard_mode", "PAUSE");
        try (ActivityScenario<PauseInterstitialActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.intervention_step_back)).check(matches(isDisplayed()));
            onView(withId(R.id.intervention_continue)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void blockHasNoContinueControl() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                PauseInterstitialActivity.class)
                .putExtra(PauseInterstitialActivity.EXTRA_TARGET_PACKAGE, "Video App")
                .putExtra("guard_mode", "BLOCK");
        try (ActivityScenario<PauseInterstitialActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.intervention_step_back)).check(matches(isDisplayed()));
            onView(withId(R.id.intervention_continue)).check(matches(not(isDisplayed())));
        }
    }
}
