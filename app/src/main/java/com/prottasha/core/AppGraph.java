package com.prottasha.core;

import android.content.Context;

public final class AppGraph {

    private final Context applicationContext;
    private final AppExecutors executors;

    public AppGraph(Context context) {
        applicationContext = context.getApplicationContext();
        executors = new AppExecutors();
    }

    public Context applicationContext() {
        return applicationContext;
    }

    public AppExecutors executors() {
        return executors;
    }
}
