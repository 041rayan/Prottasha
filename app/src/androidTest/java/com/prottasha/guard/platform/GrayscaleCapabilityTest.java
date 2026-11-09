package com.prottasha.guard.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class GrayscaleCapabilityTest {
    @Test
    public void absentSecureSettingsPermissionReturnsHonestGuidance() {
        Context context = ApplicationProvider.getApplicationContext();
        GrayscaleCapabilityController controller = new GrayscaleCapabilityController(context);
        assertFalse(controller.hasCapability());
        assertEquals(GrayscaleCapabilityController.Result.PERMISSION_REQUIRED,
                controller.setEnabled(true));
    }
}
