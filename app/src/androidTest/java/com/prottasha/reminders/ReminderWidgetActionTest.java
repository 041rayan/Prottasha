package com.prottasha.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionAccessPolicy;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.data.HabitReminderEntity;
import com.prottasha.widget.TodayWidgetProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.FutureTask;

@RunWith(AndroidJUnit4.class)
public final class ReminderWidgetActionTest {

    private ProttashaApp app;
    private SessionUser user;
    private UserDatabaseIdentity identity;
    private long buildHabitId;
    private long avoidHabitId;
    private long amountHabitId;

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        app = (ProttashaApp) context;
        user = new SessionUser("rem-" + UUID.randomUUID(), "ReminderUser", "rem@test.local", true);
        identity = UserDatabaseIdentity.fromUid(user.uid());

        onDisk(() -> {
            app.appGraph().userSessionManager().switchTo(user);
            long day = LocalDate.now().toEpochDay();
            buildHabitId = app.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().insert(
                    new HabitEntity(0L, "Morning Walk", "BUILD", 1, "DAILY",
                            day, 1, 0, false, 0, System.currentTimeMillis()));
            avoidHabitId = app.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().insert(
                    new HabitEntity(0L, "No Sugar", "AVOID", 1, "DAILY",
                            day, 1, 0, false, 1, System.currentTimeMillis()));
            amountHabitId = app.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().insert(
                    new HabitEntity(0L, "Drink Water", "AMOUNT", 4, "DAILY",
                            day, 1, 0, false, 2, System.currentTimeMillis()));
            return null;
        });
    }

    @After
    public void tearDown() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> app.appGraph().userSessionManager().signOut());
        onDisk(() -> {
            app.appGraph().userSessionDatabaseProvider().destroy(user);
            return null;
        });
    }

    @Test
    public void widgetCopyIsNeutralAndContainsNoHabitData() {
        Context context = ApplicationProvider.getApplicationContext();
        String body = context.getString(R.string.widget_neutral_body);
        assertTrue(body.contains("private") || body.contains("lock"));
        assertFalse(body.contains("lapse"));
        assertFalse(body.contains("streak"));
    }

    @Test
    public void lockedPolicyRejectsSensitiveAvoidButAllowsIdempotentTypes() {
        assertTrue(SessionAccessPolicy.canRun(
                true, true, SessionAccessPolicy.Action.COMPLETE_BUILD));
        assertTrue(SessionAccessPolicy.canRun(
                true, true, SessionAccessPolicy.Action.INCREMENT_AMOUNT));
        assertFalse(SessionAccessPolicy.canRun(
                true, true, SessionAccessPolicy.Action.CHECK_IN_AVOID));
    }

    @Test
    public void malformedReminderActionFailsClosedWithoutCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        new ReminderActionReceiver().onReceive(
                context, new Intent(context, ReminderActionReceiver.class));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void reminderSchedulerSchedulesAndCancelsAlarmsCleanly() {
        Context context = ApplicationProvider.getApplicationContext();
        ReminderScheduler scheduler = new ReminderScheduler(context);
        HabitReminderEntity reminder = new HabitReminderEntity(
                1001L, buildHabitId, 8, 30, 0, true);

        scheduler.schedule(identity.uidHash(), reminder);
        scheduler.scheduleSnooze(identity.uidHash(), 1001L, 15);
        scheduler.cancel(1001L);
    }

    @Test
    public void notificationControllerDisplaysNotificationsWithoutCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        NotificationController controller = new NotificationController(context);

        HabitEntity buildHabit = new HabitEntity(
                buildHabitId, "Morning Walk", "BUILD", 1, "DAILY", 0L, 1, 0, false, 0, 0L);
        HabitEntity avoidHabit = new HabitEntity(
                avoidHabitId, "No Sugar", "AVOID", 1, "DAILY", 0L, 1, 0, false, 1, 0L);
        HabitEntity amountHabit = new HabitEntity(
                amountHabitId, "Drink Water", "AMOUNT", 4, "DAILY", 0L, 1, 0, false, 2, 0L);

        controller.show(identity.uidHash(), buildHabit, 101L);
        controller.show(identity.uidHash(), avoidHabit, 102L);
        controller.show(identity.uidHash(), amountHabit, 103L);
    }

    @Test
    public void reminderActionReceiverExecutesBuildAndAmountAndRejectsAvoid() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        long today = LocalDate.now().toEpochDay();

        // 1. Build action (Done)
        Intent buildIntent = new Intent(context, ReminderActionReceiver.class)
                .putExtra(ReminderScheduler.EXTRA_UID_HASH, identity.uidHash())
                .putExtra(ReminderActionReceiver.EXTRA_HABIT_ID, buildHabitId)
                .putExtra(ReminderActionReceiver.EXTRA_ACTION, ReminderActionReceiver.ACTION_DONE)
                .putExtra(ReminderActionReceiver.EXTRA_NONCE, "nonce-build-1");
        new ReminderActionReceiver().onReceive(context, buildIntent);
        waitForAggregate(buildHabitId, today);

        HabitDayAggregateEntity buildAgg = onDisk(() ->
                app.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().findAggregate(buildHabitId, today));
        assertNotNull(buildAgg);
        assertTrue(buildAgg.completed);

        // 2. Amount action (Add)
        Intent amountIntent = new Intent(context, ReminderActionReceiver.class)
                .putExtra(ReminderScheduler.EXTRA_UID_HASH, identity.uidHash())
                .putExtra(ReminderActionReceiver.EXTRA_HABIT_ID, amountHabitId)
                .putExtra(ReminderActionReceiver.EXTRA_ACTION, ReminderActionReceiver.ACTION_ADD)
                .putExtra(ReminderActionReceiver.EXTRA_NONCE, "nonce-amount-1");
        new ReminderActionReceiver().onReceive(context, amountIntent);
        waitForAggregate(amountHabitId, today);

        HabitDayAggregateEntity amountAgg = onDisk(() ->
                app.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().findAggregate(amountHabitId, today));
        assertNotNull(amountAgg);
        assertEquals(1, amountAgg.value);

        // 3. Avoid action: must be rejected (stays null)
        Intent avoidIntent = new Intent(context, ReminderActionReceiver.class)
                .putExtra(ReminderScheduler.EXTRA_UID_HASH, identity.uidHash())
                .putExtra(ReminderActionReceiver.EXTRA_HABIT_ID, avoidHabitId)
                .putExtra(ReminderActionReceiver.EXTRA_ACTION, ReminderActionReceiver.ACTION_DONE)
                .putExtra(ReminderActionReceiver.EXTRA_NONCE, "nonce-avoid-1");
        new ReminderActionReceiver().onReceive(context, avoidIntent);
        Thread.sleep(200);

        HabitDayAggregateEntity avoidAgg = onDisk(() ->
                app.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().findAggregate(avoidHabitId, today));
        assertNull("Avoid habit must not be mutated via background reminder action", avoidAgg);
    }

    @Test
    public void widgetProviderUpdatesAllWidgetsWithoutCrash() {
        Context context = ApplicationProvider.getApplicationContext();
        TodayWidgetProvider.updateAllWidgets(context);
    }

    private void waitForAggregate(long habitId, long epochDay) throws Exception {
        long deadline = System.currentTimeMillis() + 3000L;
        while (System.currentTimeMillis() < deadline) {
            HabitDayAggregateEntity agg = onDisk(() ->
                    app.appGraph().userSessionDatabaseProvider().requireOpen().habitDao().findAggregate(habitId, epochDay));
            if (agg != null) return;
            Thread.sleep(30L);
        }
        throw new AssertionError("Aggregate not written for habit " + habitId);
    }

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        app.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
