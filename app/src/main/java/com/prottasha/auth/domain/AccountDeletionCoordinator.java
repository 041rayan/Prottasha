package com.prottasha.auth.domain;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prottasha.auth.data.AuthGateway;
import com.prottasha.auth.data.CloudAccountDataGateway;
import com.prottasha.core.security.EncryptedPrivateFileStore;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;

import java.util.concurrent.Executor;

public final class AccountDeletionCoordinator {

    interface LocalDataLifecycle {
        void quarantine(SessionUser user) throws Exception;
        void restore(SessionUser user) throws Exception;
        void destroy(String uidHash) throws Exception;
    }

    interface DeletionJournal {
        void save(String uidHash, DeletionState.Stage stage);
        JournalEntry load();
        void clear();
    }

    static final class JournalEntry {
        final String uidHash;
        final DeletionState.Stage stage;

        JournalEntry(String uidHash, DeletionState.Stage stage) {
            this.uidHash = uidHash;
            this.stage = stage;
        }
    }

    private final ReauthenticationCoordinator reauthentication;
    private final AuthGateway authGateway;
    private final CloudAccountDataGateway cloudGateway;
    private final LocalDataLifecycle localData;
    private final DeletionJournal journal;
    private final Runnable sessionSignOut;
    private final Executor executor;
    private final MutableLiveData<DeletionState> state = new MutableLiveData<>();
    private DeletionState currentState;
    private long generation;

    public AccountDeletionCoordinator(
            Context context,
            ReauthenticationCoordinator reauthentication,
            AuthGateway authGateway,
            CloudAccountDataGateway cloudGateway,
            UserSessionDatabaseProvider databaseProvider,
            EncryptedPrivateFileStore privateFileStore,
            UserSessionManager sessionManager,
            Executor executor) {
        this(
                reauthentication,
                authGateway,
                cloudGateway,
                new LocalDataLifecycle() {
                    @Override
                    public void quarantine(SessionUser user) throws Exception {
                        databaseProvider.quarantine(user);
                        try {
                            privateFileStore.quarantine(
                                    UserDatabaseIdentity.fromUid(user.uid()));
                        } catch (Exception error) {
                            databaseProvider.restore(user);
                            throw error;
                        }
                    }

                    @Override
                    public void restore(SessionUser user) throws Exception {
                        privateFileStore.restore(UserDatabaseIdentity.fromUid(user.uid()));
                        databaseProvider.restore(user);
                    }

                    @Override
                    public void destroy(String uidHash) throws Exception {
                        databaseProvider.destroyByUidHash(uidHash);
                        privateFileStore.destroy(UserDatabaseIdentity.fromUidHash(uidHash));
                    }
                },
                new SharedPreferencesDeletionJournal(context),
                sessionManager::signOut,
                executor);
    }

    AccountDeletionCoordinator(
            ReauthenticationCoordinator reauthentication,
            AuthGateway authGateway,
            CloudAccountDataGateway cloudGateway,
            LocalDataLifecycle localData,
            DeletionJournal journal,
            Runnable sessionSignOut,
            Executor executor) {
        this.reauthentication = reauthentication;
        this.authGateway = authGateway;
        this.cloudGateway = cloudGateway;
        this.localData = localData;
        this.journal = journal;
        this.sessionSignOut = sessionSignOut;
        this.executor = executor;
        JournalEntry pending = journal.load();
        if (pending == null) {
            publish(DeletionState.idle());
        } else if (pending.stage == DeletionState.Stage.RETRY_LOCAL_CLEANUP) {
            publish(DeletionState.at(DeletionState.Stage.RETRY_LOCAL_CLEANUP, pending.uidHash));
        } else if (pending.stage == DeletionState.Stage.RETRY_LOCAL_RESTORE) {
            publish(DeletionState.at(DeletionState.Stage.RETRY_LOCAL_RESTORE, pending.uidHash));
        } else if (pending.stage == DeletionState.Stage.DELETING_AUTH
                && authGateway.currentUser() == null) {
            try {
                journal.save(pending.uidHash, DeletionState.Stage.RETRY_LOCAL_CLEANUP);
            } catch (RuntimeException ignored) {
                // The durable DELETING_AUTH entry plus absent Firebase user remains recoverable.
            }
            publish(DeletionState.at(DeletionState.Stage.RETRY_LOCAL_CLEANUP, pending.uidHash));
        } else {
            publish(DeletionState.at(DeletionState.Stage.RESTORING_LOCAL, pending.uidHash));
        }
    }

    public LiveData<DeletionState> observeState() {
        return state;
    }

    public DeletionState currentState() {
        return currentState;
    }

    public synchronized void deleteAccount(SessionUser user, String password) {
        if (isRunning(currentState.stage())
                || currentState.stage() == DeletionState.Stage.RETRY_LOCAL_CLEANUP) {
            throw new IllegalStateException("Account deletion is already in progress");
        }
        long operation = ++generation;
        String uidHash = UserDatabaseIdentity.fromUid(user.uid()).uidHash();
        try {
            journal.save(uidHash, DeletionState.Stage.REAUTHENTICATING);
        } catch (RuntimeException journalFailure) {
            publish(DeletionState.failed(DeletionState.Failure.JOURNAL, uidHash));
            return;
        }
        publish(DeletionState.at(DeletionState.Stage.REAUTHENTICATING, uidHash));
        reauthentication.reauthenticate(user, password, result -> {
            if (!isCurrent(operation)) {
                return;
            }
            if (!result.isSuccess()) {
                journal.clear();
                publish(DeletionState.failed(mapReauthenticationFailure(result), uidHash));
                return;
            }
            executor.execute(() -> quarantine(operation, user, uidHash));
        });
    }

    public synchronized void retryLocalCleanup() {
        JournalEntry pending = journal.load();
        if (pending == null || pending.stage != DeletionState.Stage.RETRY_LOCAL_CLEANUP) {
            throw new IllegalStateException("No post-auth local cleanup is pending");
        }
        long operation = ++generation;
        publish(DeletionState.at(DeletionState.Stage.CLEANING_LOCAL, pending.uidHash));
        executor.execute(() -> destroyLocal(operation, pending.uidHash));
    }

    public synchronized void recoverInterruptedDeletion(SessionUser user) {
        JournalEntry pending = journal.load();
        String userHash = UserDatabaseIdentity.fromUid(user.uid()).uidHash();
        if (pending == null
                || pending.stage == DeletionState.Stage.RETRY_LOCAL_CLEANUP
                || !pending.uidHash.equals(userHash)) {
            throw new SecurityException("Interrupted deletion does not belong to this user");
        }
        long operation = ++generation;
        executor.execute(() -> restoreAndFail(
                operation, user, userHash, DeletionState.Failure.INTERRUPTED));
    }

    private void quarantine(long operation, SessionUser user, String uidHash) {
        if (!isCurrent(operation)) {
            return;
        }
        publish(DeletionState.at(DeletionState.Stage.QUARANTINING_LOCAL, uidHash));
        try {
            localData.quarantine(user);
        } catch (Exception error) {
            restoreAndFail(operation, user, uidHash, DeletionState.Failure.LOCAL_QUARANTINE);
            return;
        }
        try {
            journal.save(uidHash, DeletionState.Stage.DELETING_CLOUD);
        } catch (RuntimeException journalFailure) {
            restoreAndFail(operation, user, uidHash, DeletionState.Failure.JOURNAL);
            return;
        }
        publish(DeletionState.at(DeletionState.Stage.DELETING_CLOUD, uidHash));
        cloudGateway.deleteOwnedData(user.uid(), result -> {
            if (!isCurrent(operation)) {
                return;
            }
            if (!result.isSuccess()) {
                executor.execute(() -> restoreAndFail(
                        operation, user, uidHash, DeletionState.Failure.CLOUD));
                return;
            }
            deleteAuth(operation, user, uidHash);
        });
    }

    private void deleteAuth(long operation, SessionUser user, String uidHash) {
        try {
            journal.save(uidHash, DeletionState.Stage.DELETING_AUTH);
        } catch (RuntimeException journalFailure) {
            executor.execute(() -> restoreAndFail(
                    operation, user, uidHash, DeletionState.Failure.JOURNAL));
            return;
        }
        publish(DeletionState.at(DeletionState.Stage.DELETING_AUTH, uidHash));
        authGateway.deleteCurrentUser(result -> {
            if (!isCurrent(operation)) {
                return;
            }
            if (!result.isSuccess()) {
                DeletionState.Failure failure = failureCode(result)
                        == AuthGateway.FailureCode.REQUIRES_RECENT_LOGIN
                        ? DeletionState.Failure.REQUIRES_RECENT_LOGIN
                        : DeletionState.Failure.AUTH;
                executor.execute(() -> restoreAndFail(operation, user, uidHash, failure));
                return;
            }
            try {
                journal.save(uidHash, DeletionState.Stage.RETRY_LOCAL_CLEANUP);
            } catch (RuntimeException ignored) {
                // DELETING_AUTH plus absent Firebase user is sufficient for restart recovery.
            }
            publish(DeletionState.at(DeletionState.Stage.CLEANING_LOCAL, uidHash));
            try {
                sessionSignOut.run();
            } catch (RuntimeException error) {
                publish(DeletionState.at(DeletionState.Stage.RETRY_LOCAL_CLEANUP, uidHash));
                return;
            }
            executor.execute(() -> destroyLocal(operation, uidHash));
        });
    }

    private void destroyLocal(long operation, String uidHash) {
        if (!isCurrent(operation)) {
            return;
        }
        try {
            localData.destroy(uidHash);
            journal.clear();
            publish(DeletionState.at(DeletionState.Stage.COMPLETE, uidHash));
        } catch (Exception error) {
            try {
                journal.save(uidHash, DeletionState.Stage.RETRY_LOCAL_CLEANUP);
            } catch (RuntimeException ignored) {
                // Keep the observable retry state even if durable storage is temporarily unavailable.
            }
            publish(DeletionState.at(DeletionState.Stage.RETRY_LOCAL_CLEANUP, uidHash));
        }
    }

    private void restoreAndFail(
            long operation,
            SessionUser user,
            String uidHash,
            DeletionState.Failure failure) {
        if (!isCurrent(operation)) {
            return;
        }
        publish(DeletionState.at(DeletionState.Stage.RESTORING_LOCAL, uidHash));
        try {
            localData.restore(user);
            journal.clear();
            publish(DeletionState.failed(failure, uidHash));
        } catch (Exception restoreError) {
            try {
                journal.save(uidHash, DeletionState.Stage.RETRY_LOCAL_RESTORE);
            } catch (RuntimeException ignored) {
                // The preceding stage remains a conservative restore-on-restart signal.
            }
            publish(DeletionState.at(DeletionState.Stage.RETRY_LOCAL_RESTORE, uidHash));
        }
    }

    private static DeletionState.Failure mapReauthenticationFailure(AppResult<Boolean> result) {
        return failureCode(result) == AuthGateway.FailureCode.INVALID_CREDENTIALS
                ? DeletionState.Failure.INVALID_CREDENTIALS
                : DeletionState.Failure.REAUTHENTICATION;
    }

    private static AuthGateway.FailureCode failureCode(AppResult<Boolean> result) {
        Throwable error = result.errorOrNull();
        return error instanceof AuthGateway.AuthFailure
                ? ((AuthGateway.AuthFailure) error).code()
                : AuthGateway.FailureCode.UNKNOWN;
    }

    private synchronized boolean isCurrent(long operation) {
        return generation == operation;
    }

    private static boolean isRunning(DeletionState.Stage stage) {
        return stage != DeletionState.Stage.IDLE
                && stage != DeletionState.Stage.COMPLETE
                && stage != DeletionState.Stage.FAILED;
    }

    private void publish(DeletionState next) {
        currentState = next;
        state.postValue(next);
    }

    private static final class SharedPreferencesDeletionJournal implements DeletionJournal {
        private static final String PREFERENCES = "account_deletion_journal";
        private static final String UID_HASH = "uid_hash";
        private static final String STAGE = "stage";

        private final SharedPreferences preferences;

        private SharedPreferencesDeletionJournal(Context context) {
            preferences = context.getApplicationContext().getSharedPreferences(
                    PREFERENCES, Context.MODE_PRIVATE);
        }

        @Override
        public void save(String uidHash, DeletionState.Stage stage) {
            if (!preferences.edit()
                    .putString(UID_HASH, uidHash)
                    .putString(STAGE, stage.name())
                    .commit()) {
                throw new IllegalStateException("Unable to persist account deletion journal");
            }
        }

        @Override
        public JournalEntry load() {
            String uidHash = preferences.getString(UID_HASH, null);
            String stageName = preferences.getString(STAGE, null);
            if (uidHash == null || stageName == null) {
                return null;
            }
            try {
                UserDatabaseIdentity.fromUidHash(uidHash);
                return new JournalEntry(uidHash, DeletionState.Stage.valueOf(stageName));
            } catch (IllegalArgumentException invalid) {
                clear();
                return null;
            }
        }

        @Override
        public void clear() {
            if (!preferences.edit().clear().commit()) {
                throw new IllegalStateException("Unable to clear account deletion journal");
            }
        }
    }
}
