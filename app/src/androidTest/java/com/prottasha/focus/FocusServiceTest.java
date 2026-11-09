package com.prottasha.focus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.ProttashaApp;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.service.ProttashaFocusService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.FutureTask;

@RunWith(AndroidJUnit4.class)
public final class FocusServiceTest {
    private ProttashaApp application;
    private SessionUser user;

    @Before public void setUp() throws Exception {
        application = (ProttashaApp) ApplicationProvider.getApplicationContext();
        user = new SessionUser("focus-" + UUID.randomUUID(), "Focus", "focus@example.test", true);
        onDisk(() -> { application.appGraph().userSessionManager().switchTo(user); return null; });
    }

    @After public void tearDown() throws Exception {
        ApplicationProvider.getApplicationContext().stopService(
                new Intent(ApplicationProvider.getApplicationContext(), ProttashaFocusService.class));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> application.appGraph().userSessionManager().signOut());
        onDisk(() -> { application.appGraph().userSessionDatabaseProvider().destroy(user); return null; });
    }

    @Test public void visibleStartPersistsAndEndClearsSession() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        context.startForegroundService(new Intent(context, ProttashaFocusService.class)
                .setAction(ProttashaFocusService.ACTION_START)
                .putExtra(ProttashaFocusService.EXTRA_DURATION_MILLIS, 60_000L));
        waitForState("RUNNING");
        assertEquals(60_000L, application.appGraph().userSessionDatabaseProvider()
                .requireOpen().focusDao().find().durationMillis);

        context.startService(new Intent(context, ProttashaFocusService.class)
                .setAction(ProttashaFocusService.ACTION_END));
        waitForState(null);
        assertNull(application.appGraph().userSessionDatabaseProvider()
                .requireOpen().focusDao().find());
    }

    private void waitForState(String expected) throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            com.prottasha.focus.data.FocusSessionEntity state = application.appGraph()
                    .userSessionDatabaseProvider().requireOpen().focusDao().find();
            if ((state == null && expected == null)
                    || (state != null && expected != null && expected.equals(state.state))) return;
            Thread.sleep(25);
        }
        throw new AssertionError("Focus state did not become " + expected);
    }

    private <T> T onDisk(java.util.concurrent.Callable<T> callable) throws Exception {
        FutureTask<T> task = new FutureTask<>(callable);
        application.appGraph().executors().disk().execute(task);
        return task.get();
    }
}
