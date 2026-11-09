package com.prottasha.settings.account;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.domain.AccountDeletionCoordinator;
import com.prottasha.auth.domain.DeletionState;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.auth.domain.UserSessionManager;

public final class AccountViewModel extends ViewModel {

    private final UserSessionManager sessionManager;
    private final AuthRepository authRepository;
    private final AccountDeletionCoordinator deletionCoordinator;

    AccountViewModel(
            UserSessionManager sessionManager,
            AuthRepository authRepository,
            AccountDeletionCoordinator deletionCoordinator) {
        this.sessionManager = sessionManager;
        this.authRepository = authRepository;
        this.deletionCoordinator = deletionCoordinator;
        if (deletionCoordinator.currentState().stage() == DeletionState.Stage.RESTORING_LOCAL
                || deletionCoordinator.currentState().stage()
                == DeletionState.Stage.RETRY_LOCAL_RESTORE) {
            SessionUser current = currentUserOrNull();
            try {
                if (current != null) {
                    deletionCoordinator.recoverInterruptedDeletion(current);
                }
            } catch (SecurityException noMatchingSession) {
                // Recovery resumes when the matching Firebase account opens this screen.
            }
        }
    }

    public LiveData<DeletionState> deletionState() {
        return deletionCoordinator.observeState();
    }

    public boolean hasAccountSession() {
        return currentUserOrNull() != null;
    }

    private SessionUser currentUserOrNull() {
        try {
            return sessionManager.requireUser();
        } catch (IllegalStateException noSession) {
            return authRepository.currentState().userOrNull();
        }
    }

    public void deleteAccount(String password) {
        SessionUser user = currentUserOrNull();
        if (user == null) {
            throw new IllegalStateException("No Firebase account is active");
        }
        deletionCoordinator.deleteAccount(user, password);
    }

    public void retryPendingWork() {
        if (deletionCoordinator.currentState().stage()
                == DeletionState.Stage.RETRY_LOCAL_RESTORE) {
            SessionUser current = currentUserOrNull();
            if (current == null) {
                throw new IllegalStateException("No Firebase account is active");
            }
            deletionCoordinator.recoverInterruptedDeletion(current);
        } else {
            deletionCoordinator.retryLocalCleanup();
        }
    }

    public static final class Factory implements ViewModelProvider.Factory {
        private final UserSessionManager sessionManager;
        private final AuthRepository authRepository;
        private final AccountDeletionCoordinator deletionCoordinator;

        public Factory(
                UserSessionManager sessionManager,
                AuthRepository authRepository,
                AccountDeletionCoordinator deletionCoordinator) {
            this.sessionManager = sessionManager;
            this.authRepository = authRepository;
            this.deletionCoordinator = deletionCoordinator;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.isAssignableFrom(AccountViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new AccountViewModel(
                    sessionManager, authRepository, deletionCoordinator));
        }
    }
}
