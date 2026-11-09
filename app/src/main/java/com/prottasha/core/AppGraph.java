package com.prottasha.core;

import android.content.Context;

import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.data.CloudUserProfileRepository;
import com.prottasha.auth.data.FirebaseAuthGateway;
import com.prottasha.auth.domain.UserSessionManager;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;

public final class AppGraph {

    private final Context applicationContext;
    private final AppExecutors executors;
    private final AuthRepository authRepository;
    private final CloudUserProfileRepository cloudUserProfileRepository;
    private final UserSessionDatabaseProvider userSessionDatabaseProvider;
    private final UserSessionManager userSessionManager;

    public AppGraph(Context context) {
        applicationContext = context.getApplicationContext();
        executors = new AppExecutors();
        authRepository = new AuthRepository(new FirebaseAuthGateway());
        cloudUserProfileRepository = new CloudUserProfileRepository();
        userSessionDatabaseProvider = new UserSessionDatabaseProvider(applicationContext);
        userSessionManager = new UserSessionManager(userSessionDatabaseProvider, authRepository);
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
}
