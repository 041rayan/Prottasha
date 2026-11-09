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
    private final NetworkPowerStateRepository networkPowerStateRepository;
    private final InterventionEventRepository interventionEventRepository;

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
        networkPowerStateRepository = new NetworkPowerStateRepository();
        interventionEventRepository = new InterventionEventRepository(
                userSessionDatabaseProvider, executors.disk());
        encryptedPrivateFileStore = new EncryptedPrivateFileStore(applicationContext);
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

    public IdentityRepository identityRepository() {
        return identityRepository;
    }

    public HabitRepository habitRepository() {
        return habitRepository;
    }

    public GuardRuleRepository guardRuleRepository() {
        return guardRuleRepository;
    }

    public NetworkPowerStateRepository networkPowerState() {
        return networkPowerStateRepository;
    }

    public InterventionEventRepository interventionEvents() {
        return interventionEventRepository;
    }
}
