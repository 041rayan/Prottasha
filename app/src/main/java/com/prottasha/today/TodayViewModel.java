package com.prottasha.today;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.core.AppGraph;
import com.prottasha.core.result.ResultCallback;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.data.GuardRuleRepository;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.data.HabitRepository;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.habits.ui.HabitConsistencyGridBuilder;
import com.prottasha.habits.ui.HabitConsistencyGridState;
import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.identity.data.IdentityRepository;
import com.prottasha.identity.data.SuccessMemoryEntity;
import com.prottasha.intervention.data.InterventionEventEntity;
import com.prottasha.intervention.platform.AccessibilityServiceHealth;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public final class TodayViewModel extends ViewModel {

    private static final int CONSISTENCY_WEEKS = 12;

    private final IdentityRepository identityRepository;
    private final HabitRepository habitRepository;
    private final GuardRuleRepository guardRuleRepository;
    private final UserSessionDatabaseProvider databaseProvider;
    private final AccessibilityServiceHealth serviceHealthChecker;
    private final Executor diskExecutor;

    private final MutableLiveData<AccessibilityServiceHealth.State> serviceHealth =
            new MutableLiveData<>(AccessibilityServiceHealth.State.HEALTHY);
    private final MutableLiveData<HopeHorizonState> hopeHorizon =
            new MutableLiveData<>(new HopeHorizonState(0, 0, 0));
    private final MutableLiveData<HabitConsistencyGridState> consistency =
            new MutableLiveData<>(HabitConsistencyGridBuilder.build(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    LocalDate.now(),
                    CONSISTENCY_WEEKS));

    @VisibleForTesting
    public TodayViewModel(
            IdentityRepository identityRepository,
            @Nullable HabitRepository habitRepository,
            @Nullable GuardRuleRepository guardRuleRepository,
            @Nullable UserSessionDatabaseProvider databaseProvider,
            @Nullable AccessibilityServiceHealth serviceHealthChecker,
            @Nullable Executor diskExecutor) {
        this.identityRepository = identityRepository;
        this.habitRepository = habitRepository;
        this.guardRuleRepository = guardRuleRepository;
        this.databaseProvider = databaseProvider;
        this.serviceHealthChecker = serviceHealthChecker;
        this.diskExecutor = diskExecutor;
        refresh();
    }

    TodayViewModel(IdentityRepository identityRepository) {
        this(identityRepository, null, null, null, null, null);
    }

    public LiveData<IdentityEntity> activeIdentity() {
        return identityRepository.observeActive();
    }

    public LiveData<SuccessMemoryEntity> latestSuccessMemory() {
        return identityRepository.observeLatestSuccessMemory();
    }

    public LiveData<List<HabitModels.HabitTodayItem>> dueHabits() {
        if (habitRepository != null) {
            return habitRepository.observeDueOn(LocalDate.now().toEpochDay());
        }
        return new MutableLiveData<>(Collections.emptyList());
    }

    public LiveData<List<GuardRuleEntity>> activeRules() {
        if (guardRuleRepository != null) {
            return guardRuleRepository.observeRules();
        }
        return new MutableLiveData<>(Collections.emptyList());
    }

    public LiveData<AccessibilityServiceHealth.State> serviceHealth() {
        return serviceHealth;
    }

    public LiveData<HopeHorizonState> hopeHorizon() {
        return hopeHorizon;
    }

    public LiveData<HabitConsistencyGridState> consistency() {
        return consistency;
    }

    public void refresh() {
        refreshHealth();
        refreshHopeHorizon();
        refreshConsistency();
    }

    public void refreshHealth() {
        if (serviceHealthChecker != null) {
            AccessibilityServiceHealth.State state = serviceHealthChecker.state(0L, false);
            serviceHealth.postValue(state);
        }
    }

    public void refreshHopeHorizon() {
        if (databaseProvider == null || diskExecutor == null) return;
        diskExecutor.execute(() -> {
            try {
                UserDatabase db = databaseProvider.requireOpen();
                List<InterventionEventEntity> events = db.interventionDao().recent(50);
                if (events == null || events.isEmpty()) {
                    hopeHorizon.postValue(new HopeHorizonState(0, 0, 0));
                    return;
                }
                int mindfulExits = 0;
                int intentionalContinues = 0;
                int prompts = 0;
                for (InterventionEventEntity event : events) {
                    if ("STEP_BACK".equalsIgnoreCase(event.type) || "FOLLOW_THROUGH".equalsIgnoreCase(event.type)) {
                        mindfulExits++;
                        prompts++;
                    } else if ("CONTINUE".equalsIgnoreCase(event.type)) {
                        intentionalContinues++;
                        prompts++;
                    } else if ("BLOCKED".equalsIgnoreCase(event.type) || "SHOWN".equalsIgnoreCase(event.type)) {
                        prompts++;
                    }
                }
                if (prompts == 0 && !events.isEmpty()) {
                    prompts = events.size();
                }
                hopeHorizon.postValue(new HopeHorizonState(prompts, mindfulExits, intentionalContinues));
            } catch (Exception ignored) {
                // User database might not be initialized or currently switching
            }
        });
    }

    public void refreshConsistency() {
        if (databaseProvider == null || diskExecutor == null) return;
        diskExecutor.execute(() -> {
            try {
                UserDatabase db = databaseProvider.requireOpen();
                List<HabitEntity> habits = db.habitDao().listActive();
                LocalDate today = LocalDate.now();
                long fromDay = today.minusWeeks(CONSISTENCY_WEEKS).toEpochDay();
                for (HabitEntity habit : habits) {
                    fromDay = Math.min(fromDay, habit.startEpochDay);
                }
                List<HabitDayAggregateEntity> aggregates = db.habitDao()
                        .listAllAggregatesBetween(fromDay, today.toEpochDay());
                consistency.postValue(HabitConsistencyGridBuilder.build(
                        habits, aggregates, today, CONSISTENCY_WEEKS));
            } catch (Exception ignored) {
                // User database might not be initialized or currently switching.
            }
        });
    }

    public void recordHabitAction(HabitAction action, ResultCallback<HabitModels.CompletionResult> callback) {
        if (habitRepository != null) {
            habitRepository.record(action, callback);
        }
    }

    public static final class Factory implements ViewModelProvider.Factory {
        private final IdentityRepository identityRepository;
        private final HabitRepository habitRepository;
        private final GuardRuleRepository guardRuleRepository;
        private final UserSessionDatabaseProvider databaseProvider;
        private final AccessibilityServiceHealth serviceHealthChecker;
        private final Executor diskExecutor;

        public Factory(IdentityRepository identityRepository) {
            this(identityRepository, null, null, null, null, null);
        }

        public Factory(AppGraph graph) {
            this(
                    graph.identityRepository(),
                    graph.habitRepository(),
                    graph.guardRuleRepository(),
                    graph.userSessionDatabaseProvider(),
                    new AccessibilityServiceHealth(graph.applicationContext()),
                    graph.executors().disk()
            );
        }

        public Factory(
                IdentityRepository identityRepository,
                @Nullable HabitRepository habitRepository,
                @Nullable GuardRuleRepository guardRuleRepository,
                @Nullable UserSessionDatabaseProvider databaseProvider,
                @Nullable AccessibilityServiceHealth serviceHealthChecker,
                @Nullable Executor diskExecutor) {
            this.identityRepository = identityRepository;
            this.habitRepository = habitRepository;
            this.guardRuleRepository = guardRuleRepository;
            this.databaseProvider = databaseProvider;
            this.serviceHealthChecker = serviceHealthChecker;
            this.diskExecutor = diskExecutor;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.isAssignableFrom(TodayViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new TodayViewModel(
                    identityRepository,
                    habitRepository,
                    guardRuleRepository,
                    databaseProvider,
                    serviceHealthChecker,
                    diskExecutor));
        }
    }
}
