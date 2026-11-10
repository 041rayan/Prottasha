package com.prottasha.core;

import android.content.Context;

import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.data.CloudUserProfileRepository;
import com.prottasha.auth.data.FirebaseAuthGateway;
import com.prottasha.auth.data.FirebaseCloudAccountDataGateway;
import com.prottasha.auth.domain.AccountDeletionCoordinator;
import com.prottasha.auth.domain.ReauthenticationCoordinator;
import com.prottasha.auth.domain.UserSessionManager;
import com.prottasha.core.security.EncryptedPrivateFileStore;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.identity.data.IdentityRepository;
import com.prottasha.habits.data.HabitRepository;
import com.prottasha.guard.data.GuardRuleRepository;
import com.prottasha.core.platform.NetworkPowerStateRepository;
import com.prottasha.intervention.data.InterventionEventRepository;
import com.prottasha.settings.privacy.LocalDeletionService;

import com.prottasha.insights.data.InsightsRepository;
import com.prottasha.triggers.data.TriggerRepository;
import com.prottasha.usage.data.UsageRepository;

public final class AppGraph {

    private final Context applicationContext;
    private final AppExecutors executors;
    private final AuthRepository authRepository;
    private final CloudUserProfileRepository cloudUserProfileRepository;
    private final UserSessionDatabaseProvider userSessionDatabaseProvider;
    private final UserSessionManager userSessionManager;
    private final EncryptedPrivateFileStore encryptedPrivateFileStore;
    private final AccountDeletionCoordinator accountDeletionCoordinator;
    private final IdentityRepository identityRepository;
    private final HabitRepository habitRepository;
    private final GuardRuleRepository guardRuleRepository;
    private final com.prottasha.guard.data.GuardRuntimeRepository guardRuntimeRepository;
    private final NetworkPowerStateRepository networkPowerStateRepository;
    private final InterventionEventRepository interventionEventRepository;
    private final LocalDeletionService localDeletionService;
    private final InsightsRepository insightsRepository;
    private final UsageRepository usageRepository;
    private final TriggerRepository triggerRepository;

    public AppGraph(Context context) {
        applicationContext = context.getApplicationContext();
        executors = new AppExecutors();
        FirebaseAuthGateway firebaseAuthGateway = new FirebaseAuthGateway();
        authRepository = new AuthRepository(firebaseAuthGateway);
        cloudUserProfileRepository = new CloudUserProfileRepository();
        userSessionDatabaseProvider = new UserSessionDatabaseProvider(applicationContext);
        userSessionManager = new UserSessionManager(userSessionDatabaseProvider, authRepository);
        identityRepository = new IdentityRepository(userSessionDatabaseProvider);
        habitRepository = new HabitRepository(userSessionDatabaseProvider, executors.disk());
        guardRuleRepository = new GuardRuleRepository(
                userSessionDatabaseProvider, executors.disk());
        guardRuntimeRepository = new com.prottasha.guard.data.GuardRuntimeRepository();
        networkPowerStateRepository = new NetworkPowerStateRepository();
        interventionEventRepository = new InterventionEventRepository(
                userSessionDatabaseProvider, executors.disk());
        insightsRepository = new InsightsRepository(userSessionDatabaseProvider);
        usageRepository = new UsageRepository(userSessionDatabaseProvider, executors.disk());
        triggerRepository = new TriggerRepository(userSessionDatabaseProvider);
        encryptedPrivateFileStore = new EncryptedPrivateFileStore(applicationContext);
        localDeletionService = new LocalDeletionService(
                userSessionDatabaseProvider, encryptedPrivateFileStore);
        accountDeletionCoordinator = new AccountDeletionCoordinator(
                applicationContext,
                new ReauthenticationCoordinator(firebaseAuthGateway),
                firebaseAuthGateway,
                new FirebaseCloudAccountDataGateway(),
                userSessionDatabaseProvider,
                encryptedPrivateFileStore,
                userSessionManager,
                executors.disk());
    }

    public Context applicationContext() {
        return applicationContext;
    }

    public AppExecutors executors() {
        return executors;
    }

    public AuthRepository authRepository() {
        return authRepository;
    }

    public CloudUserProfileRepository cloudUserProfileRepository() {
        return cloudUserProfileRepository;
    }

    public UserSessionDatabaseProvider userSessionDatabaseProvider() {
        return userSessionDatabaseProvider;
    }

    public UserSessionManager userSessionManager() {
        return userSessionManager;
    }

    public EncryptedPrivateFileStore encryptedPrivateFileStore() {
        return encryptedPrivateFileStore;
    }

    public AccountDeletionCoordinator accountDeletionCoordinator() {
        return accountDeletionCoordinator;
    }

    public LocalDeletionService localDeletionService() {
        return localDeletionService;
    }

    public IdentityRepository identityRepository() {
        return identityRepository;
    }

    public HabitRepository habitRepository() {
        return habitRepository;
    }

    public GuardRuleRepository guardRuleRepository() {
        return guardRuleRepository;
    }

    public com.prottasha.guard.data.GuardRuntimeRepository guardRuntime() {
        return guardRuntimeRepository;
    }

    public NetworkPowerStateRepository networkPowerState() {
        return networkPowerStateRepository;
    }

    public InterventionEventRepository interventionEvents() {
        return interventionEventRepository;
    }

    public InsightsRepository insightsRepository() {
        return insightsRepository;
    }

    public UsageRepository usageRepository() {
        return usageRepository;
    }

    public TriggerRepository triggerRepository() {
        return triggerRepository;
    }
}
