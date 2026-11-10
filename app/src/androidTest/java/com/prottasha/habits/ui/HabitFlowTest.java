package com.prottasha.habits.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.view.View;

import androidx.core.widget.NestedScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.ui.main.MainActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.List;
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
            waitForView(withText("Read ten pages"), 5000);
            onView(withText("Read ten pages")).check(matches(isDisplayed()));
            onView(withText("Avoid doomscrolling")).check(matches(isDisplayed()));
            onView(withText("Drink water")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void filterChipsFilterHabitsByType() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.nav_habits)).perform(click());
            waitForView(withText("Read ten pages"), 5000);

            // Filter BUILD
            onView(withId(R.id.habits_filter_build)).perform(click());
            waitForView(withText("Read ten pages"), 3000);
            onView(withText("Read ten pages")).check(matches(isDisplayed()));

            // Filter AVOID
            onView(withId(R.id.habits_filter_avoid)).perform(click());
            waitForView(withText("Avoid doomscrolling"), 3000);
            onView(withText("Avoid doomscrolling")).check(matches(isDisplayed()));

            // Filter AMOUNT
            onView(withId(R.id.habits_filter_amount)).perform(click());
            waitForView(withText("Drink water"), 3000);
            onView(withText("Drink water")).check(matches(isDisplayed()));

            // Reset ALL
            onView(withId(R.id.habits_filter_all)).perform(click());
            waitForView(withText("Read ten pages"), 3000);
            onView(withText("Read ten pages")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void editorSavesAnAmountHabitWithProgressiveDisclosure() throws Exception {
        try (ActivityScenario<HabitEditActivity> ignored =
                     ActivityScenario.launch(HabitEditActivity.class)) {
            onView(withId(R.id.habit_title_input)).perform(replaceText("Practice guitar"));
            onView(withId(R.id.habit_type_amount)).perform(click());
            onView(withId(R.id.habit_target_input)).perform(replaceText("20"));
            onView(withId(R.id.habit_unit_input)).perform(scrollToInNested(), replaceText("mins"));
            onView(withId(R.id.habit_increment_input)).perform(scrollToInNested(), replaceText("5"));
            onView(withId(R.id.habit_cue_input)).perform(scrollToInNested(), replaceText("Guitar stand in living room"));
            onView(withId(R.id.habit_save_button)).perform(scrollToInNested(), click());
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
        assertEquals("mins", saved.unit);
        assertEquals(5, saved.incrementVal);
        assertEquals("Guitar stand in living room", saved.cue);
    }

    @Test
    public void editorShowsUnsavedConfirmationOnBackWhenDirty() {
        try (ActivityScenario<HabitEditActivity> ignored =
                     ActivityScenario.launch(HabitEditActivity.class)) {
            onView(withId(R.id.habit_title_input)).perform(replaceText("Draft habit"));
            pressBack();
            waitForView(withText(R.string.habit_unsaved_title), 3000);
            onView(withText(R.string.habit_unsaved_title)).check(matches(isDisplayed()));
            onView(withText(R.string.habit_discard)).perform(click());
        }
    }

    @Test
    public void habitDetailShowsStatsCalendarAndBackfill() throws Exception {
        HabitEntity habit = application.appGraph().userSessionDatabaseProvider()
                .requireOpen().habitDao().listActive().get(0);
        assertNotNull(habit);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HabitDetailActivity.class);
        intent.putExtra(HabitDetailActivity.EXTRA_HABIT_ID, habit.id);

        try (ActivityScenario<HabitDetailActivity> ignored = ActivityScenario.launch(intent)) {
            waitForView(withId(R.id.habit_history_title), 5000);
            onView(withId(R.id.habit_history_title)).check(matches(withText(habit.title)));
            onView(withId(R.id.habit_history_calendar)).check(matches(isDisplayed()));

            // Backfill yesterday
            onView(withId(R.id.habit_backfill_button)).perform(scrollToInNested(), click());
            waitForView(withText("Yesterday"), 3000);
            onView(withText("Yesterday")).perform(click());
            waitForView(withText("Mark Done"), 3000);
            onView(withText("Mark Done")).perform(click());
        }

        long yesterday = LocalDate.now().toEpochDay() - 1;
        waitForAggregate(habit.id, yesterday);
        HabitDayAggregateEntity aggregate = application.appGraph().userSessionDatabaseProvider()
                .requireOpen().habitDao().findAggregate(habit.id, yesterday);
        assertNotNull(aggregate);
        assertTrue(aggregate.completed);
        assertEquals(1, aggregate.value);
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
        throw new AssertionError("Habit count target not reached");
    }

    private void waitForAggregate(long habitId, long epochDay) throws Exception {
        long deadline = System.currentTimeMillis() + 3000L;
        while (System.currentTimeMillis() < deadline) {
            if (application.appGraph().userSessionDatabaseProvider()
                    .requireOpen().habitDao().findAggregate(habitId, epochDay) != null) {
                return;
            }
            Thread.sleep(25L);
        }
        throw new AssertionError("Aggregate not written");
    }

    private static void waitForView(Matcher<View> matcher, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                onView(matcher).check(matches(isDisplayed()));
                return;
            } catch (Throwable notYet) {
                android.os.SystemClock.sleep(50);
            }
        }
        onView(matcher).check(matches(isDisplayed()));
    }

    private static ViewAction scrollToInNested() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "scroll to view in NestedScrollView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View parent = (View) view.getParent();
                while (parent != null && !(parent instanceof NestedScrollView)) {
                    Object p = parent.getParent();
                    parent = p instanceof View ? (View) p : null;
                }
                if (parent instanceof NestedScrollView) {
                    int[] viewLocation = new int[2];
                    int[] parentLocation = new int[2];
                    view.getLocationOnScreen(viewLocation);
                    parent.getLocationOnScreen(parentLocation);
                    int dy = viewLocation[1] - parentLocation[1];
                    ((NestedScrollView) parent).scrollBy(0, dy);
                }
                view.requestRectangleOnScreen(new Rect(0, 0, view.getWidth(), view.getHeight()), true);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        application.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
