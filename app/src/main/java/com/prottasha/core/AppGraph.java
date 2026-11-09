package com.prottasha.core;

import android.content.Context;

import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.data.CloudUserProfileRepository;
import com.prottasha.auth.data.FirebaseAuthGateway;

public final class AppGraph {

    private final Context applicationContext;
    private final AppExecutors executors;
    private final AuthRepository authRepository;
    private final CloudUserProfileRepository cloudUserProfileRepository;

    public AppGraph(Context context) {
        applicationContext = context.getApplicationContext();
        executors = new AppExecutors();
        authRepository = new AuthRepository(new FirebaseAuthGateway());
        cloudUserProfileRepository = new CloudUserProfileRepository();
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
}
