package com.prottasha.checklist;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChecklistComplianceTest {

    @Test
    public void verifyMultipleActivitiesExist() throws ClassNotFoundException {
        // Navigation and architecture: Multiple Activities
        assertNotNull(Class.forName("com.prottasha.ui.splash.SplashActivity"));
        assertNotNull(Class.forName("com.prottasha.ui.main.MainActivity"));
        assertNotNull(Class.forName("com.prottasha.ui.detail.HabitDetailActivity"));
        assertNotNull(Class.forName("com.prottasha.ui.detail.TriggerDetailActivity"));
        assertNotNull(Class.forName("com.prottasha.ui.pause.PauseInterstitialActivity"));
        assertNotNull(Class.forName("com.prottasha.ui.crisis.CrisisHelpActivity"));
        assertNotNull(Class.forName("com.prottasha.ui.pod.PodChatActivity"));
        assertNotNull(Class.forName("com.prottasha.ui.settings.SettingsActivity"));
    }

    @Test
    public void verifyAndroidComponentsExist() throws ClassNotFoundException {
        // Broadcast Receiver
        assertNotNull(Class.forName("com.prottasha.receiver.BatteryOrAirplaneReceiver"));

        // Foreground Service
        assertNotNull(Class.forName("com.prottasha.service.ProttashaFocusService"));

        // Accessibility Service
        assertNotNull(Class.forName("com.prottasha.service.ProttashaAccessibilityService"));
    }

    @Test
    public void verifyRecyclerViewAdaptersExist() throws ClassNotFoundException {
        // UI and lists: RecyclerView with Adapter and ViewHolder
        assertNotNull(Class.forName("com.prottasha.ui.adapter.HabitAdapter"));
        assertNotNull(Class.forName("com.prottasha.ui.adapter.TriggerAdapter"));
        assertNotNull(Class.forName("com.prottasha.ui.adapter.TherapistAdapter"));
        assertNotNull(Class.forName("com.prottasha.ui.adapter.PodMessageAdapter"));
    }

    @Test
    public void verifyNetworkingAndFirebaseManagersExist() throws ClassNotFoundException {
        // Networking & Firebase
        assertNotNull(Class.forName("com.prottasha.data.remote.QuoteApiClient"));
        assertNotNull(Class.forName("com.prottasha.data.remote.TherapistApiClient"));
        assertNotNull(Class.forName("com.prottasha.data.firebase.FirebaseAuthManager"));
        assertNotNull(Class.forName("com.prottasha.data.firebase.FirebaseRealtimeManager"));
        assertNotNull(Class.forName("com.prottasha.data.firebase.FirebasePodManager"));
        assertNotNull(Class.forName("com.prottasha.util.ImageStorageHelper"));
        assertNotNull(Class.forName("com.prottasha.util.GrayscaleManager"));
    }

    @Test
    public void verifyRoomDatabaseAndDaosExist() throws ClassNotFoundException {
        // Local SQLite Room Database
        assertNotNull(Class.forName("com.prottasha.data.local.AppDatabase"));
        assertNotNull(Class.forName("com.prottasha.data.local.dao.IdentityDao"));
        assertNotNull(Class.forName("com.prottasha.data.local.dao.HabitDao"));
        assertNotNull(Class.forName("com.prottasha.data.local.dao.TriggerDao"));
        assertNotNull(Class.forName("com.prottasha.data.local.dao.JournalDao"));
        assertNotNull(Class.forName("com.prottasha.data.local.dao.AssessmentDao"));
    }
}
