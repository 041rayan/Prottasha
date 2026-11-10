package com.prottasha.settings.account;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.auth.data.AuthGateway;
import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.domain.AccountDeletionCoordinator;
import com.prottasha.auth.domain.DeletionState;
import com.prottasha.auth.domain.PasswordPolicy;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.auth.domain.UserSessionManager;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.settings.privacy.LocalDeletionService;

public final class AccountViewModel extends ViewModel {

    private final UserSessionManager sessionManager;
    private final AuthRepository authRepository;
    @Nullable
    private final AccountDeletionCoordinator deletionCoordinator;
    @Nullable
    private final LocalDeletionService localDeletionService;

    public AccountViewModel(
            UserSessionManager sessionManager,
            AuthRepository authRepository,
            @Nullable AccountDeletionCoordinator deletionCoordinator,
            @Nullable LocalDeletionService localDeletionService) {
        this.sessionManager = sessionManager;
        this.authRepository = authRepository;
        this.deletionCoordinator = deletionCoordinator;
        this.localDeletionService = localDeletionService;
        if (deletionCoordinator != null) {
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
    }

    AccountViewModel(
            UserSessionManager sessionManager,
            AuthRepository authRepository,
            AccountDeletionCoordinator deletionCoordinator) {
        this(sessionManager, authRepository, deletionCoordinator, null);
    }

    public LiveData<DeletionState> deletionState() {
        if (deletionCoordinator != null) {
            return deletionCoordinator.observeState();
        }
        return new MutableLiveData<>(DeletionState.idle());
    }

    public boolean hasAccountSession() {
        return currentUserOrNull() != null;
    }

    public SessionUser currentUserOrNull() {
        try {
            return sessionManager.requireUser();
        } catch (IllegalStateException noSession) {
            return authRepository.currentState().userOrNull();
        }
    }

    public void signOut() {
        sessionManager.signOut();
    }

    public void signOutAndRemoveLocalData(ResultCallback<Boolean> callback) {
        SessionUser user = currentUserOrNull();
        if (user == null) {
            callback.onResult(AppResult.failure(new IllegalStateException("No active session")));
            return;
        }
        String uidHash = UserDatabaseIdentity.fromUid(user.uid()).uidHash();
        sessionManager.signOut();
        if (localDeletionService != null) {
            try {
                localDeletionService.delete(uidHash);
                callback.onResult(AppResult.success(true));
            } catch (Exception e) {
                callback.onResult(AppResult.failure(e));
            }
        } else {
            callback.onResult(AppResult.success(true));
        }
    }

    public void changePassword(
            String currentPassword,
            String newPassword,
            ResultCallback<Boolean> callback) {
        SessionUser user = currentUserOrNull();
        if (user == null || user.email() == null) {
            callback.onResult(AppResult.failure(new IllegalStateException("No active session")));
            return;
        }
        if (PasswordPolicy.validate(newPassword) != PasswordPolicy.Result.VALID) {
            callback.onResult(AppResult.failure(
                    new AuthGateway.AuthFailure(AuthGateway.FailureCode.WEAK_PASSWORD)));
            return;
        }
        authRepository.reauthenticate(user.email(), currentPassword, reauthResult -> {
            if (!reauthResult.isSuccess()) {
                callback.onResult(reauthResult);
                return;
            }
            authRepository.updatePassword(newPassword, callback);
        });
    }

    public void changeEmail(
            String currentPassword,
            String newEmail,
            ResultCallback<Boolean> callback) {
        SessionUser user = currentUserOrNull();
        if (user == null || user.email() == null) {
            callback.onResult(AppResult.failure(new IllegalStateException("No active session")));
            return;
        }
        authRepository.reauthenticate(user.email(), currentPassword, reauthResult -> {
            if (!reauthResult.isSuccess()) {
                callback.onResult(reauthResult);
                return;
            }
            authRepository.updateEmail(newEmail, callback);
        });
    }

    public void deleteAccount(String password) {
        if (deletionCoordinator == null) {
            throw new IllegalStateException("Deletion coordinator is not configured");
        }
        SessionUser user = currentUserOrNull();
        if (user == null) {
            throw new IllegalStateException("No Firebase account is active");
        }
        deletionCoordinator.deleteAccount(user, password);
    }

    public void retryPendingWork() {
        if (deletionCoordinator == null) {
            return;
        }
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
        @Nullable
        private final LocalDeletionService localDeletionService;

        public Factory(
                UserSessionManager sessionManager,
                AuthRepository authRepository,
                AccountDeletionCoordinator deletionCoordinator,
                @Nullable LocalDeletionService localDeletionService) {
            this.sessionManager = sessionManager;
            this.authRepository = authRepository;
            this.deletionCoordinator = deletionCoordinator;
            this.localDeletionService = localDeletionService;
        }

        public Factory(
                UserSessionManager sessionManager,
                AuthRepository authRepository,
                AccountDeletionCoordinator deletionCoordinator) {
            this(sessionManager, authRepository, deletionCoordinator, null);
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.isAssignableFrom(AccountViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new AccountViewModel(
                    sessionManager, authRepository, deletionCoordinator, localDeletionService));
        }
    }
}
