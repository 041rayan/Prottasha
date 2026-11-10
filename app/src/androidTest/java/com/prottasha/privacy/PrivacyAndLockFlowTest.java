package com.prottasha.privacy;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
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
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.insights.data.JournalEntryEntity;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.settings.privacy.LogicalArchiveRepository;
import com.prottasha.settings.privacy.PrivacyActivity;
import com.prottasha.settings.security.AppLockActivity;
import com.prottasha.settings.security.AppLockPolicy;
import com.prottasha.settings.security.AppLockSetupActivity;
import com.prottasha.settings.security.PinCredentialStore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.FutureTask;

@RunWith(AndroidJUnit4.class)
public final class PrivacyAndLockFlowTest {

    private ProttashaApp app;
    private SessionUser user;
    private String uidHash;

    @Before public void setUp() throws Exception {
        app = (ProttashaApp) ApplicationProvider.getApplicationContext();
        user = new SessionUser("privacy-" + UUID.randomUUID(), "Privacy User", "p@example.test", true);
        uidHash = UserDatabaseIdentity.fromUid(user.uid()).uidHash();
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

    @Test public void privacyActivityRendersLockAndExportOptions() {
        try (ActivityScenario<PrivacyActivity> ignored = ActivityScenario.launch(PrivacyActivity.class)) {
            onView(withId(R.id.privacy_lock_setup_button)).perform(scrollToInNested()).check(matches(isDisplayed()));
            onView(withId(R.id.privacy_password)).perform(scrollToInNested()).check(matches(isDisplayed()));
            onView(withId(R.id.privacy_export)).perform(scrollToInNested()).check(matches(isDisplayed()));
            onView(withId(R.id.privacy_import_password)).perform(scrollToInNested()).check(matches(isDisplayed()));
            onView(withId(R.id.privacy_import)).perform(scrollToInNested()).check(matches(isDisplayed()));
        }
    }

    @Test public void appLockSetupActivityConfiguresPinAndPersistsPolicy() {
        try (ActivityScenario<AppLockSetupActivity> ignored = ActivityScenario.launch(AppLockSetupActivity.class)) {
            onView(withId(R.id.app_lock_new_pin_input)).perform(scrollToInNested(), replaceText("4829"), closeSoftKeyboard());
            onView(withId(R.id.app_lock_confirm_pin_input)).perform(scrollToInNested(), replaceText("4829"), closeSoftKeyboard());
            onView(withId(R.id.app_lock_save_pin_button)).perform(scrollToInNested(), click());

            AppLockPolicy policy = AppLockPolicy.load(app, uidHash);
            assertTrue(policy.isLockEnabled());
            assertTrue(policy.hasPin());
        }
    }

    @Test public void appLockActivityUnlocksWithCorrectPin() throws Exception {
        // Pre-configure policy with PIN "1234"
        AppLockPolicy policy = AppLockPolicy.load(app, uidHash);
        policy.setPinVerifier(PinCredentialStore.create("1234".toCharArray()));
        policy.setLockEnabled(true);
        policy.save(app, uidHash);

        try (ActivityScenario<AppLockActivity> scenario = ActivityScenario.launch(AppLockActivity.class)) {
            onView(withId(R.id.btn_num_1)).perform(click());
            onView(withId(R.id.btn_num_2)).perform(click());
            onView(withId(R.id.btn_num_3)).perform(click());
            onView(withId(R.id.btn_num_4)).perform(click());

            // Activity should finish on success
            Thread.sleep(300);
            assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test public void logicalArchiveRoundTripsRealRoomDatabaseData() throws Exception {
        // Insert test records
        onDisk(() -> {
            UserDatabase db = app.appGraph().userSessionDatabaseProvider().requireOpen();
            HabitEntity h = new HabitEntity(
                    0L, "Nightly Reading", "BUILD", 1, "DAILY", 20000L,
                    1, 1, false, 0, System.currentTimeMillis(),
                    "After dinner", "Read 1 page", "Peace of mind", "If sleepy then audio",
                    "Resume next night", "pages", 1, 10, 0, "Self"
            );
            db.habitDao().insert(h);

            JournalEntryEntity j = new JournalEntryEntity(
                    0L, 20000L, "Urge noticed", "Boredom trigger", "Walk outside", System.currentTimeMillis()
            );
            db.insightsDao().insert(j);
            return null;
        });

        // Export archive to memory
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        onDisk(() -> {
            UserDatabase db = app.appGraph().userSessionDatabaseProvider().requireOpen();
            LogicalArchiveRepository repo = new LogicalArchiveRepository();
            repo.exportArchive(db, app.appGraph().encryptedPrivateFileStore(), uidHash, out, "SecurePass123".toCharArray());
            return null;
        });

        byte[] archiveBytes = out.toByteArray();
        assertTrue(archiveBytes.length > 0);

        // Wipe database
        onDisk(() -> {
            UserDatabase db = app.appGraph().userSessionDatabaseProvider().requireOpen();
            db.clearAllTables();
            assertEquals(0, db.habitDao().listAll().size());
            assertEquals(0, db.insightsDao().recent(10).size());
            return null;
        });

        // Import archive
        onDisk(() -> {
            UserDatabase db = app.appGraph().userSessionDatabaseProvider().requireOpen();
            LogicalArchiveRepository repo = new LogicalArchiveRepository();
            ByteArrayInputStream in = new ByteArrayInputStream(archiveBytes);
            LogicalArchiveRepository.ImportSummary summary = repo.importArchive(
                    db, app.appGraph().encryptedPrivateFileStore(), uidHash, in, "SecurePass123".toCharArray());

            assertTrue(summary.habitsRestored >= 1);
            assertTrue(summary.reflectionsRestored >= 1);
            assertEquals("Nightly Reading", db.habitDao().listAll().get(0).title);
            assertEquals("Urge noticed", db.insightsDao().recent(1).get(0).fact);
            return null;
        });
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
                        NestedScrollView nsv = (NestedScrollView) parent;
                        int top = 0;
                        View curr = view;
                        while (curr != null && curr != nsv) {
                            top += curr.getTop();
                            ViewParent p = curr.getParent();
                            curr = (p instanceof View) ? (View) p : null;
                        }
                        nsv.scrollTo(0, top);
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
