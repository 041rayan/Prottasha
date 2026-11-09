package com.prottasha.auth.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class UserSessionManagerTest {

    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void switchAtoBtoAClosesBeforeEachNewOwner() throws Exception {
        FakeDatabaseLifecycle database = new FakeDatabaseLifecycle();
        UserSessionManager manager = new UserSessionManager(
                database, () -> database.events.add("firebase-sign-out"));

        manager.switchTo(user("a", true));
        manager.switchTo(user("b", true));
        manager.switchTo(user("a", true));

        assertEquals(Arrays.asList(
                        "clear-owner", "open:a",
                        "close:a", "clear-owner", "open:b",
                        "close:b", "clear-owner", "open:a"),
                database.events);
        assertEquals("a", manager.requireUser().uid());
        assertEquals("a", database.activeUid);
        assertFalse(database.openedWhileAnotherOwnerWasActive);
    }

    @Test
    public void signOutClosesAndClearsBeforeFirebaseSignOut() throws Exception {
        FakeDatabaseLifecycle database = new FakeDatabaseLifecycle();
        UserSessionManager manager = new UserSessionManager(
                database, () -> database.events.add("firebase-sign-out"));
        manager.switchTo(user("a", true));
        database.events.clear();

        manager.signOut();

        assertEquals(Arrays.asList("close:a", "clear-owner", "firebase-sign-out"),
                database.events);
        assertThrows(IllegalStateException.class, manager::requireUser);
    }

    @Test
    public void sameOwnerReopensAfterTrustedBackgroundClose() throws Exception {
        FakeDatabaseLifecycle database = new FakeDatabaseLifecycle();
        UserSessionManager manager = new UserSessionManager(database, () -> { });
        manager.switchTo(user("a", true));
        database.close();
        database.events.clear();

        manager.switchTo(user("a", true));

        assertEquals(Arrays.asList("open:a"), database.events);
        assertEquals("a", database.activeUid);
        assertEquals("a", manager.requireUser().uid());
    }

    @Test
    public void failedSwitchDoesNotRestoreOrPublishTheOldOwner() throws Exception {
        FakeDatabaseLifecycle database = new FakeDatabaseLifecycle();
        UserSessionManager manager = new UserSessionManager(database, () -> { });
        List<String> observed = new ArrayList<>();
        Observer<SessionUser> observer = user -> observed.add(user == null ? "none" : user.uid());
        manager.observeUser().observeForever(observer);
        manager.switchTo(user("a", true));
        database.failUid = "b";

        assertThrows(IOException.class, () -> manager.switchTo(user("b", true)));

        assertThrows(IllegalStateException.class, manager::requireUser);
        assertEquals(Arrays.asList("a", "none"), observed);
        assertEquals(null, database.activeUid);
        manager.observeUser().removeObserver(observer);
    }

    @Test
    public void removedObserverReceivesNoLaterOwner() throws Exception {
        FakeDatabaseLifecycle database = new FakeDatabaseLifecycle();
        UserSessionManager manager = new UserSessionManager(database, () -> { });
        List<String> observed = new ArrayList<>();
        Observer<SessionUser> observer = user -> observed.add(user.uid());
        manager.observeUser().observeForever(observer);

        manager.switchTo(user("a", true));
        manager.observeUser().removeObserver(observer);
        manager.switchTo(user("b", true));

        assertEquals(Arrays.asList("a"), observed);
    }

    @Test
    public void unverifiedUserCannotOpenOrBecomeCurrent() {
        FakeDatabaseLifecycle database = new FakeDatabaseLifecycle();
        UserSessionManager manager = new UserSessionManager(database, () -> { });

        assertThrows(SecurityException.class, () -> manager.switchTo(user("a", false)));

        assertTrue(database.events.isEmpty());
        assertThrows(IllegalStateException.class, manager::requireUser);
    }

    private static SessionUser user(String uid, boolean verified) {
        return new SessionUser(uid, uid, uid + "@example.test", verified);
    }

    private static final class FakeDatabaseLifecycle
            implements UserSessionManager.DatabaseLifecycle {
        private final List<String> events = new ArrayList<>();
        private String activeUid;
        private String failUid;
        private boolean openedWhileAnotherOwnerWasActive;

        @Override
        public void open(SessionUser user) throws IOException {
            events.add("open:" + user.uid());
            if (activeUid != null) {
                openedWhileAnotherOwnerWasActive = true;
            }
            if (user.uid().equals(failUid)) {
                throw new IOException("planned open failure");
            }
            activeUid = user.uid();
        }

        @Override
        public void close() {
            if (activeUid != null) {
                events.add("close:" + activeUid);
            }
            activeUid = null;
        }

        @Override
        public void clearVerifiedBackgroundOwner() {
            events.add("clear-owner");
        }
    }
}
