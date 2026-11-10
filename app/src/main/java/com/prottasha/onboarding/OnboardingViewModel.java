package com.prottasha.onboarding;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.data.GuardRuleRepository;
import com.prottasha.identity.data.IdentityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public final class OnboardingViewModel extends ViewModel {

    public static final int CURRENT_VERSION = 2;

    public enum Stage {
        PURPOSE,
        IDENTITY,
        TARGETS,
        RULES,
        DISCLOSURE,
        PERMISSIONS,
        INTERVENTION_PREVIEW
    }

    private static final String PURPOSE = "purpose";
    private static final String IDENTITY = "identity";
    private static final String SUCCESS_MEMORY = "success_memory";
    private static final String SELECTED_PACKAGES = "selected_packages";
    private static final String TARGET_MODE = "target_mode";

    public static final String EXTRA_INITIAL_VERSION = "extra_initial_version";

    private final SavedStateHandle savedState;
    @Nullable
    private final UserSessionDatabaseProvider databaseProvider;
    @Nullable
    private final IdentityRepository identityRepository;
    @Nullable
    private final GuardRuleRepository guardRuleRepository;
    @Nullable
    private final Executor diskExecutor;
    private final int initialVersion;
    private final MutableLiveData<Boolean> finished = new MutableLiveData<>();
    private final MutableLiveData<Stage> startStage = new MutableLiveData<>();

    public OnboardingViewModel(
            SavedStateHandle savedState,
            @Nullable UserSessionDatabaseProvider databaseProvider,
            @Nullable IdentityRepository identityRepository,
            @Nullable GuardRuleRepository guardRuleRepository,
            @Nullable Executor diskExecutor,
            int initialVersion) {
        this.savedState = savedState;
        this.databaseProvider = databaseProvider;
        this.identityRepository = identityRepository;
        this.guardRuleRepository = guardRuleRepository;
        this.diskExecutor = diskExecutor;
        this.initialVersion = initialVersion;
        if (initialVersion >= 0) {
            startStage.setValue(initialVersion >= 1 ? Stage.TARGETS : Stage.PURPOSE);
        } else if (diskExecutor != null && databaseProvider != null) {
            diskExecutor.execute(() -> {
                int v = 0;
                try {
                    v = databaseProvider.requireOpen().localAccountDao().requireAccount().onboardingVersion;
                } catch (Exception ignored) {}
                startStage.postValue(v >= 1 ? Stage.TARGETS : Stage.PURPOSE);
            });
        } else {
            startStage.setValue(Stage.PURPOSE);
        }
    }

    public Stage initialStage() {
        return initialVersion >= 1 ? Stage.TARGETS : Stage.PURPOSE;
    }

    public LiveData<Stage> startStage() {
        return startStage;
    }

    public OnboardingDraft draft() {
        ArrayList<String> pkgs = savedState.get(SELECTED_PACKAGES);
        return new OnboardingDraft(
                savedState.get(PURPOSE),
                savedState.get(IDENTITY),
                savedState.get(SUCCESS_MEMORY),
                pkgs,
                savedState.get(TARGET_MODE));
    }

    public void setPurpose(String purpose) {
        savedState.set(PURPOSE, purpose);
    }

    public void setIdentityStatement(String statement) {
        savedState.set(IDENTITY, statement);
    }

    public void setSuccessMemory(String memory) {
        savedState.set(SUCCESS_MEMORY, memory);
    }

    public void setSelectedPackages(List<String> packages) {
        savedState.set(SELECTED_PACKAGES, packages == null ? new ArrayList<>() : new ArrayList<>(packages));
    }

    public void setTargetMode(String mode) {
        savedState.set(TARGET_MODE, mode);
    }

    public LiveData<Boolean> finished() {
        return finished;
    }

    public void complete(boolean skipped) {
        OnboardingDraft draft = draft();
        if (diskExecutor != null) {
            diskExecutor.execute(() -> persistCompletion(draft, skipped));
        } else {
            finished.postValue(true);
        }
    }

    private void persistCompletion(OnboardingDraft draft, boolean skipped) {
        if (!skipped && identityRepository != null && !draft.identityStatement.trim().isEmpty()) {
            identityRepository.saveActive(draft.identityStatement, draft.purpose, draft.successMemory);
        }

        if (!skipped && guardRuleRepository != null && !draft.selectedPackages.isEmpty()) {
            String mode = draft.targetMode != null ? draft.targetMode : "PAUSE";
            for (String pkg : draft.selectedPackages) {
                GuardRuleEntity entity = new GuardRuleEntity(
                        0L, pkg, pkg, mode, 0, 0, 0, true);
                guardRuleRepository.save(entity, r -> {});
            }
        }

        if (databaseProvider != null) {
            try {
                databaseProvider.requireOpen().localAccountDao()
                        .updateOnboardingVersion(CURRENT_VERSION);
            } catch (Exception ignored) {}
        }
        finished.postValue(true);
    }

    @SuppressWarnings("deprecation")
    public static final class Factory extends AbstractSavedStateViewModelFactory {
        private final UserSessionDatabaseProvider databaseProvider;
        private final IdentityRepository identityRepository;
        private final GuardRuleRepository guardRuleRepository;
        private final Executor diskExecutor;
        private final int initialVersion;

        public Factory(
                SavedStateRegistryOwner owner,
                Bundle defaultArgs,
                UserSessionDatabaseProvider databaseProvider,
                IdentityRepository identityRepository,
                GuardRuleRepository guardRuleRepository,
                Executor diskExecutor,
                int initialVersion) {
            super(owner, defaultArgs);
            this.databaseProvider = databaseProvider;
            this.identityRepository = identityRepository;
            this.guardRuleRepository = guardRuleRepository;
            this.diskExecutor = diskExecutor;
            this.initialVersion = initialVersion;
        }

        public Factory(
                SavedStateRegistryOwner owner,
                Bundle defaultArgs,
                UserSessionDatabaseProvider databaseProvider,
                IdentityRepository identityRepository,
                Executor diskExecutor) {
            this(owner, defaultArgs, databaseProvider, identityRepository, null, diskExecutor, 0);
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
                    handle, databaseProvider, identityRepository, guardRuleRepository, diskExecutor, initialVersion));
        }
    }
}
