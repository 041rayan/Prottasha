package com.prottasha.auth.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prottasha.auth.data.AuthRepository;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;

public final class UserSessionManager {

    interface DatabaseLifecycle {
        void open(SessionUser user) throws IOException, GeneralSecurityException;
        void close();
        void clearVerifiedBackgroundOwner();
    }

    private final DatabaseLifecycle database;
    private final Runnable firebaseSignOut;
    private final MutableLiveData<SessionUser> userState = new MutableLiveData<>();
    private SessionUser currentUser;

    public UserSessionManager(
            UserSessionDatabaseProvider databaseProvider,
            AuthRepository authRepository) {
        this(new DatabaseLifecycle() {
            @Override
            public void open(SessionUser user) throws IOException, GeneralSecurityException {
                databaseProvider.open(user);
            }

            @Override
            public void close() {
                databaseProvider.close();
            }

            @Override
            public void clearVerifiedBackgroundOwner() {
                databaseProvider.clearVerifiedBackgroundOwner();
            }
        }, authRepository::signOut);
    }

    UserSessionManager(DatabaseLifecycle database, Runnable firebaseSignOut) {
        this.database = database;
        this.firebaseSignOut = firebaseSignOut;
    }

    public synchronized SessionUser switchTo(SessionUser user)
            throws IOException, GeneralSecurityException {
        if (user == null || !user.isEmailVerified()) {
            throw new SecurityException("Verified Firebase account required");
        }
        if (currentUser != null && currentUser.uid().equals(user.uid())) {
            database.open(user);
            return currentUser;
        }

        if (currentUser != null) {
            database.close();
            currentUser = null;
            userState.postValue(null);
        }
        database.clearVerifiedBackgroundOwner();
        database.open(user);
        currentUser = user;
        userState.postValue(user);
        return user;
    }

    public synchronized SessionUser requireUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No verified user session is active");
        }
        return currentUser;
    }

    public LiveData<SessionUser> observeUser() {
        return userState;
    }

    public synchronized void signOut() {
        database.close();
        database.clearVerifiedBackgroundOwner();
        if (currentUser != null) {
            currentUser = null;
            userState.postValue(null);
        }
        firebaseSignOut.run();
    }
}
