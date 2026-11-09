package com.prottasha.habits.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.ui.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.FutureTask;

@RunWith(AndroidJUnit4.class)
public final class HabitFlowTest {

    private ProttashaApp application;
    private SessionUser user;

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        application = (ProttashaApp) context;
        user = new SessionUser("habit-" + UUID.randomUUID(), "Habit", "habit@example.test", true);
        onDisk(() -> {
            application.appGraph().userSessionManager().switchTo(user);
            application.appGraph().userSessionDatabaseProvider().requireOpen()
                    .localAccountDao().updateOnboardingVersion(OnboardingViewModel.CURRENT_VERSION);
            long day = LocalDate.now().toEpochDay();
            application.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().insert(
                    new HabitEntity(0L, "Read ten pages", "BUILD", 1, "DAILY",
                            day, 1, 0, false, 0, System.currentTimeMillis()));
            application.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().insert(
                    new HabitEntity(0L, "Avoid doomscrolling", "AVOID", 1, "DAILY",
                            day, 1, 0, false, 1, System.currentTimeMillis()));
            application.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().insert(
                    new HabitEntity(0L, "Drink water", "AMOUNT", 3, "DAILY",
                            day, 1, 0, false, 2, System.currentTimeMillis()));
            return null;
        });
    }

    @After
    public void tearDown() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> application.appGraph().userSessionManager().signOut());
        onDisk(() -> {
            application.appGraph().userSessionDatabaseProvider().destroy(user);
            return null;
        });
    }

    @Test
    public void habitsTabShowsBuildAvoidAndAmountWithoutSampleMetrics() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.nav_habits)).perform(click());
            onView(withText("Read ten pages")).check(matches(isDisplayed()));
            onView(withText("Avoid doomscrolling")).check(matches(isDisplayed()));
            onView(withText("Drink water")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void editorSavesAnAmountHabit() throws Exception {
        try (ActivityScenario<HabitEditActivity> ignored =
                     ActivityScenario.launch(HabitEditActivity.class)) {
            onView(withId(R.id.habit_title_input)).perform(replaceText("Practice guitar"));
            onView(withId(R.id.habit_type_amount)).perform(click());
            onView(withId(R.id.habit_target_input)).perform(replaceText("20"));
            onView(withId(R.id.habit_save_button)).perform(click());
        }
        waitForHabitCount(4);
        HabitEntity saved = null;
        for (HabitEntity habit : application.appGraph().userSessionDatabaseProvider()
                .requireOpen().habitDao().listActive()) {
            if ("Practice guitar".equals(habit.title)) {
                saved = habit;
                break;
            }
        }
        if (saved == null) {
            throw new AssertionError("Saved habit was not found");
        }
        assertEquals("Practice guitar", saved.title);
        assertEquals("AMOUNT", saved.type);
        assertEquals(20, saved.target);
    }

    private void waitForHabitCount(int count) throws Exception {
        long deadline = System.currentTimeMillis() + 3000L;
        while (System.currentTimeMillis() < deadline) {
            if (application.appGraph().userSessionDatabaseProvider()
                    .requireOpen().habitDao().listActive().size() >= count) {
                return;
            }
            Thread.sleep(25L);
        }
        throw new AssertionError("Habit save did not complete");
    }

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        application.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
