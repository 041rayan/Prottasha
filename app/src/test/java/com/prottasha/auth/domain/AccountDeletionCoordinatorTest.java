package com.prottasha.auth.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.prottasha.auth.data.AuthGateway;
import com.prottasha.auth.data.CloudAccountDataGateway;
import com.prottasha.auth.data.FakeAuthGateway;
import com.prottasha.core.result.AppResult;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AccountDeletionCoordinatorTest {

    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void wrongPasswordStopsBeforeAnyDestructiveStage() {
        Fixture fixture = new Fixture();
        fixture.auth.reauthenticationResult = failure(AuthGateway.FailureCode.INVALID_CREDENTIALS);

        fixture.coordinator.deleteAccount(fixture.user, "wrong password");

        assertEquals(DeletionState.Stage.FAILED, fixture.coordinator.currentState().stage());
        assertEquals(DeletionState.Failure.INVALID_CREDENTIALS,
                fixture.coordinator.currentState().failureOrNull());
        assertEquals(Arrays.asList("reauth"), fixture.events);
        assertNull(fixture.journal.load());
    }

    @Test
    public void successfulDeletionOrdersCloudAuthAndKeyDestruction() {
        Fixture fixture = new Fixture();

        fixture.coordinator.deleteAccount(fixture.user, "Strong123");

        assertEquals(Arrays.asList(
                "reauth", "quarantine", "cloud-profile-delete", "auth-delete",
                "session-sign-out", "destroy-local"), fixture.events);
        assertEquals(DeletionState.Stage.COMPLETE, fixture.coordinator.currentState().stage());
        assertNull(fixture.journal.load());
        assertEquals(fixture.user.uid(), fixture.cloud.deletedUid);
        assertTrue(fixture.cloud.profileOnly);
    }

    @Test
    public void missingCloudProfileIsAnIdempotentSuccess() {
        Fixture fixture = new Fixture();
        fixture.cloud.profileWasMissing = true;

        fixture.coordinator.deleteAccount(fixture.user, "Strong123");

        assertEquals(DeletionState.Stage.COMPLETE, fixture.coordinator.currentState().stage());
        assertEquals(1, fixture.cloud.deleteCalls);
        assertEquals(1, fixture.auth.deleteCalls);
    }

    @Test
    public void cloudFailureRestoresQuarantineAndKeepsAuth() {
        Fixture fixture = new Fixture();
        fixture.cloud.result = failure(AuthGateway.FailureCode.NETWORK);

        fixture.coordinator.deleteAccount(fixture.user, "Strong123");

        assertEquals(Arrays.asList(
                "reauth", "quarantine", "cloud-profile-delete", "restore-local"),
                fixture.events);
        assertEquals(DeletionState.Failure.CLOUD,
                fixture.coordinator.currentState().failureOrNull());
        assertEquals(0, fixture.auth.deleteCalls);
        assertFalse(fixture.local.destroyed);
        assertNull(fixture.journal.load());
    }

    @Test
    public void recentLoginFailureRestoresLocalData() {
        Fixture fixture = new Fixture();
        fixture.auth.deleteResult = failure(AuthGateway.FailureCode.REQUIRES_RECENT_LOGIN);

        fixture.coordinator.deleteAccount(fixture.user, "Strong123");

        assertEquals(Arrays.asList(
                "reauth", "quarantine", "cloud-profile-delete", "auth-delete",
                "restore-local"), fixture.events);
        assertEquals(DeletionState.Failure.REQUIRES_RECENT_LOGIN,
                fixture.coordinator.currentState().failureOrNull());
        assertFalse(fixture.local.destroyed);
    }

    @Test
    public void postAuthCleanupFailureSurvivesReconstructionAndRetriesLocallyOnly() {
        Fixture fixture = new Fixture();
        fixture.local.failDestroy = true;

        fixture.coordinator.deleteAccount(fixture.user, "Strong123");

        assertEquals(DeletionState.Stage.RETRY_LOCAL_CLEANUP,
                fixture.coordinator.currentState().stage());
        assertEquals(DeletionState.Stage.RETRY_LOCAL_CLEANUP, fixture.journal.load().stage);
        int remoteEvents = fixture.events.size();

        fixture.local.failDestroy = false;
        AccountDeletionCoordinator reconstructed = fixture.newCoordinator();
        assertEquals(DeletionState.Stage.RETRY_LOCAL_CLEANUP,
                reconstructed.currentState().stage());
        reconstructed.retryLocalCleanup();

        assertEquals(DeletionState.Stage.COMPLETE, reconstructed.currentState().stage());
        assertEquals(remoteEvents + 1, fixture.events.size());
        assertEquals("destroy-local", fixture.events.get(fixture.events.size() - 1));
        assertEquals(1, fixture.cloud.deleteCalls);
        assertEquals(1, fixture.auth.deleteCalls);
        assertNull(fixture.journal.load());
    }

    @Test
    public void retryWithoutPostAuthJournalIsRejected() {
        Fixture fixture = new Fixture();

        assertThrows(IllegalStateException.class, fixture.coordinator::retryLocalCleanup);
    }

    @Test
    public void postAuthSessionResetFailureRemainsLocallyRetryable() {
        Fixture fixture = new Fixture();
        fixture.failSessionSignOut = true;

        fixture.coordinator.deleteAccount(fixture.user, "Strong123");

        assertEquals(DeletionState.Stage.RETRY_LOCAL_CLEANUP,
                fixture.coordinator.currentState().stage());
        assertEquals(DeletionState.Stage.RETRY_LOCAL_CLEANUP, fixture.journal.load().stage);
        assertFalse(fixture.local.destroyed);
    }

    @Test
    public void reconstructionBeforeAuthDeletionRequiresLocalRestore() {
        Fixture fixture = new Fixture();
        String uidHash = com.prottasha.core.security.UserDatabaseIdentity
                .fromUid(fixture.user.uid()).uidHash();
        fixture.journal.save(uidHash, DeletionState.Stage.DELETING_CLOUD);

        AccountDeletionCoordinator reconstructed = fixture.newCoordinator();
        assertEquals(DeletionState.Stage.RESTORING_LOCAL,
                reconstructed.currentState().stage());

        reconstructed.recoverInterruptedDeletion(fixture.user);

        assertEquals(Arrays.asList("restore-local"), fixture.events);
        assertEquals(DeletionState.Stage.FAILED, reconstructed.currentState().stage());
        assertEquals(DeletionState.Failure.INTERRUPTED,
                reconstructed.currentState().failureOrNull());
        assertNull(fixture.journal.load());
    }

    @Test
    public void failedRestoreKeepsARepairableJournal() {
        Fixture fixture = new Fixture();
        fixture.cloud.result = failure(AuthGateway.FailureCode.NETWORK);
        fixture.local.failRestore = true;

        fixture.coordinator.deleteAccount(fixture.user, "Strong123");

        assertEquals(DeletionState.Stage.RETRY_LOCAL_RESTORE,
                fixture.coordinator.currentState().stage());
        assertEquals(DeletionState.Stage.RETRY_LOCAL_RESTORE, fixture.journal.load().stage);

        fixture.local.failRestore = false;
        fixture.coordinator.recoverInterruptedDeletion(fixture.user);

        assertEquals(DeletionState.Stage.FAILED, fixture.coordinator.currentState().stage());
        assertNull(fixture.journal.load());
    }

    @Test
    public void authDeletionJournalWithNoFirebaseUserBecomesCleanupRetry() {
        Fixture fixture = new Fixture();
        String uidHash = com.prottasha.core.security.UserDatabaseIdentity
                .fromUid(fixture.user.uid()).uidHash();
        fixture.journal.save(uidHash, DeletionState.Stage.DELETING_AUTH);
        fixture.auth.currentUser = null;

        AccountDeletionCoordinator reconstructed = fixture.newCoordinator();

        assertEquals(DeletionState.Stage.RETRY_LOCAL_CLEANUP,
                reconstructed.currentState().stage());
        assertEquals(DeletionState.Stage.RETRY_LOCAL_CLEANUP, fixture.journal.load().stage);
    }

    @Test
    public void journalFailureAfterQuarantineRestoresBeforeStopping() {
        Fixture fixture = new Fixture();
        fixture.journal.failStage = DeletionState.Stage.DELETING_CLOUD;

        fixture.coordinator.deleteAccount(fixture.user, "Strong123");

        assertEquals(Arrays.asList("reauth", "quarantine", "restore-local"),
                fixture.events);
        assertEquals(DeletionState.Failure.JOURNAL,
                fixture.coordinator.currentState().failureOrNull());
    }

    private static AppResult<Boolean> failure(AuthGateway.FailureCode code) {
        return AppResult.failure(new AuthGateway.AuthFailure(code));
    }

    private static final class Fixture {
        private final List<String> events = new ArrayList<>();
        private final SessionUser user = new SessionUser(
                "firebase-uid-a", "Alice", "alice@example.test", true);
        private final FakeAuthGateway auth = new FakeAuthGateway();
        private final FakeCloud cloud = new FakeCloud(events);
        private final FakeLocal local = new FakeLocal(events);
        private final FakeJournal journal = new FakeJournal();
        private final AccountDeletionCoordinator coordinator;
        private boolean failSessionSignOut;

        private Fixture() {
            auth.events = events;
            auth.reauthenticationResult = AppResult.success(true);
            auth.deleteResult = AppResult.success(true);
            coordinator = newCoordinator();
        }

        private AccountDeletionCoordinator newCoordinator() {
            return new AccountDeletionCoordinator(
                    new ReauthenticationCoordinator(auth),
                    auth,
                    cloud,
                    local,
                    journal,
                    () -> {
                        events.add("session-sign-out");
                        if (failSessionSignOut) {
                            throw new IllegalStateException("planned session reset failure");
                        }
                    },
                    Runnable::run);
        }
    }

    private static final class FakeCloud implements CloudAccountDataGateway {
        private final List<String> events;
        private AppResult<Boolean> result = AppResult.success(true);
        private String deletedUid;
        private boolean profileOnly;
        private boolean profileWasMissing;
        private int deleteCalls;

        private FakeCloud(List<String> events) {
            this.events = events;
        }

        @Override
        public void deleteOwnedData(String uid, com.prottasha.core.result.ResultCallback<Boolean> callback) {
            events.add("cloud-profile-delete");
            deleteCalls++;
            deletedUid = uid;
            profileOnly = true;
            callback.onResult(profileWasMissing ? AppResult.success(false) : result);
        }
    }

    private static final class FakeLocal
            implements AccountDeletionCoordinator.LocalDataLifecycle {
        private final List<String> events;
        private boolean failDestroy;
        private boolean failRestore;
        private boolean destroyed;

        private FakeLocal(List<String> events) {
            this.events = events;
        }

        @Override
        public void quarantine(SessionUser user) {
            events.add("quarantine");
        }

        @Override
        public void restore(SessionUser user) {
            events.add("restore-local");
            if (failRestore) {
                throw new IllegalStateException("planned restore failure");
            }
        }

        @Override
        public void destroy(String uidHash) throws IOException {
            events.add("destroy-local");
            if (failDestroy) {
                throw new IOException("planned cleanup failure");
            }
            destroyed = true;
        }
    }

    private static final class FakeJournal
            implements AccountDeletionCoordinator.DeletionJournal {
        private AccountDeletionCoordinator.JournalEntry entry;
        private DeletionState.Stage failStage;

        @Override
        public void save(String uidHash, DeletionState.Stage stage) {
            if (stage == failStage) {
                throw new IllegalStateException("planned journal failure");
            }
            entry = new AccountDeletionCoordinator.JournalEntry(uidHash, stage);
        }

        @Override
        public AccountDeletionCoordinator.JournalEntry load() {
            return entry;
        }

        @Override
        public void clear() {
            entry = null;
        }
    }
}
