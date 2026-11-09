package com.prottasha.onboarding;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.identity.data.IdentityRepository;

import java.util.concurrent.Executor;

public final class OnboardingViewModel extends ViewModel {

    public static final int CURRENT_VERSION = 1;
    private static final String PURPOSE = "purpose";
    private static final String IDENTITY = "identity";

    private final SavedStateHandle savedState;
    private final UserSessionDatabaseProvider databaseProvider;
    private final IdentityRepository identityRepository;
    private final Executor diskExecutor;
    private final MutableLiveData<Boolean> finished = new MutableLiveData<>();

    OnboardingViewModel(
            SavedStateHandle savedState,
            UserSessionDatabaseProvider databaseProvider,
            IdentityRepository identityRepository,
            Executor diskExecutor) {
        this.savedState = savedState;
        this.databaseProvider = databaseProvider;
        this.identityRepository = identityRepository;
        this.diskExecutor = diskExecutor;
    }

    public OnboardingDraft draft() {
        return new OnboardingDraft(savedState.get(PURPOSE), savedState.get(IDENTITY));
    }

    public void setPurpose(String purpose) {
        savedState.set(PURPOSE, purpose);
    }

    public void setIdentityStatement(String statement) {
        savedState.set(IDENTITY, statement);
    }

    public LiveData<Boolean> finished() {
        return finished;
    }

    public void complete(boolean skipped) {
        OnboardingDraft draft = draft();
        diskExecutor.execute(() -> {
            if (!skipped && !draft.identityStatement.trim().isEmpty()) {
                identityRepository.saveActive(draft.identityStatement, draft.purpose);
            }
            databaseProvider.requireOpen().localAccountDao()
                    .updateOnboardingVersion(CURRENT_VERSION);
            finished.postValue(true);
        });
    }

    @SuppressWarnings("deprecation")
    public static final class Factory extends AbstractSavedStateViewModelFactory {
        private final UserSessionDatabaseProvider databaseProvider;
        private final IdentityRepository identityRepository;
        private final Executor diskExecutor;

        public Factory(
                SavedStateRegistryOwner owner,
                Bundle defaultArgs,
                UserSessionDatabaseProvider databaseProvider,
                IdentityRepository identityRepository,
                Executor diskExecutor) {
            super(owner, defaultArgs);
            this.databaseProvider = databaseProvider;
            this.identityRepository = identityRepository;
            this.diskExecutor = diskExecutor;
        }

        @NonNull
        @Override
        protected <T extends ViewModel> T create(
                @NonNull String key,
                @NonNull Class<T> modelClass,
                @NonNull SavedStateHandle handle) {
            if (!modelClass.isAssignableFrom(OnboardingViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new OnboardingViewModel(
                    handle, databaseProvider, identityRepository, diskExecutor));
        }
    }
}
