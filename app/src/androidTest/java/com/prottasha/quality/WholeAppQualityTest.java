package com.prottasha.quality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import com.google.android.material.button.MaterialButton;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.settings.privacy.PrivacyActivity;
import com.prottasha.ui.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.FutureTask;

@RunWith(AndroidJUnit4.class)
public final class WholeAppQualityTest {
    private ProttashaApp app;
    private SessionUser user;

    @Before public void setUp() throws Exception {
        app = (ProttashaApp) ApplicationProvider.getApplicationContext();
        user = new SessionUser("quality-" + UUID.randomUUID(), "Quality", "q@example.test", true);
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

    @Test public void shellHasFiveDestinationsAndAccessibleHeaderTargets() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                BottomNavigationView navigation = activity.findViewById(R.id.main_bottom_navigation);
                assertEquals(5, navigation.getMenu().size());
                float density = activity.getResources().getDisplayMetrics().density;
                MaterialButton lifeline = activity.findViewById(R.id.main_lifeline_button);
                MaterialButton settings = activity.findViewById(R.id.main_settings_button);
                assertTrue(lifeline.getHeight() >= 48 * density);
                assertTrue(settings.getHeight() >= 48 * density);
                assertTrue(TextUtils.isEmpty(lifeline.getText()));
                assertTrue(TextUtils.isEmpty(settings.getText()));
                assertTrue(lifeline.getIcon() != null);
                assertTrue(settings.getIcon() != null);
                assertFalse(TextUtils.isEmpty(lifeline.getContentDescription()));
                assertFalse(TextUtils.isEmpty(settings.getContentDescription()));
                assertFalse(TextUtils.isEmpty(activity.findViewById(
                        R.id.main_section_title).getContentDescription()));
            });
        }
    }

    @Test public void backupsAreDisabledAndPrivacyCopyIsHonest() throws Exception {
        ApplicationInfo info = app.getPackageManager().getApplicationInfo(app.getPackageName(), 0);
        assertFalse((info.flags & ApplicationInfo.FLAG_ALLOW_BACKUP) != 0);
        try (ActivityScenario<PrivacyActivity> scenario = ActivityScenario.launch(PrivacyActivity.class)) {
            scenario.onActivity(activity -> {
                android.widget.TextView copy = activity.findViewById(R.id.privacy_lock_copy);
                assertTrue(copy.getText().toString().contains("screen privacy"));
            });
        }
    }

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        app.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
