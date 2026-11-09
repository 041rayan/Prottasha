package com.prottasha.foundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;
import com.prottasha.ProttashaApp;
import com.prottasha.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ApplicationBootstrapTest {

    @Test
    public void applicationCreatesOneStableGraphAndInitializesFirebase() {
        Context context = ApplicationProvider.getApplicationContext();

        assertTrue(context instanceof ProttashaApp);
        ProttashaApp application = (ProttashaApp) context;
        assertNotNull(application.appGraph());
        assertSame(application.appGraph(), application.appGraph());
        assertSame(application.appGraph().executors().disk(),
                application.appGraph().executors().disk());
        assertSame(application.appGraph().executors().calculation(),
                application.appGraph().executors().calculation());
        assertSame(application.appGraph().userSessionDatabaseProvider(),
                application.appGraph().userSessionDatabaseProvider());
        assertSame(application.appGraph().userSessionManager(),
                application.appGraph().userSessionManager());
        assertSame(application.appGraph().encryptedPrivateFileStore(),
                application.appGraph().encryptedPrivateFileStore());
        assertSame(application.appGraph().accountDeletionCoordinator(),
                application.appGraph().accountDeletionCoordinator());
        assertTrue(FirebaseApp.getApps(context).size() > 0);
    }

    @Test
    public void hopeActionUsesAccessibleDarkForeground() {
        Application application = ApplicationProvider.getApplicationContext();

        assertEquals(0xFF090C11,
                ContextCompat.getColor(application, R.color.color_on_hope));
    }
}
