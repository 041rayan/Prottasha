package com.prottasha.reminders;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.R;
import com.prottasha.auth.domain.SessionAccessPolicy;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ReminderWidgetActionTest {

    @Test
    public void widgetCopyIsNeutralAndContainsNoHabitData() {
        Context context = ApplicationProvider.getApplicationContext();
        String body = context.getString(R.string.widget_neutral_body);
        assertTrue(body.contains("private"));
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
}
