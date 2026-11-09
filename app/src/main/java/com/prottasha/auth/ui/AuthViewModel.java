package com.prottasha.auth.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.data.CloudUserProfileRepository;
import com.prottasha.auth.domain.AuthState;
import com.prottasha.auth.domain.SessionUser;

public final class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final CloudUserProfileRepository profileRepository;
    private String profileBootstrapUid;

    AuthViewModel(
            AuthRepository authRepository,
            CloudUserProfileRepository profileRepository) {
        this.authRepository = authRepository;
        this.profileRepository = profileRepository;
    }

    public LiveData<AuthState> state() {
        return authRepository.observeState();
    }

    public AuthState currentState() {
        return authRepository.currentState();
    }

    public void createAccount(String displayName, String email, String password) {
        authRepository.createAccount(displayName, email, password);
    }

    public void signIn(String email, String password) {
        authRepository.signIn(email, password);
    }

    public void sendVerification() {
        authRepository.sendVerification();
    }

    public void refreshVerification() {
        authRepository.refreshVerification();
    }

    public void sendPasswordReset(String email) {
        authRepository.sendPasswordReset(email);
    }

    public void signOut() {
        profileBootstrapUid = null;
        authRepository.signOut();
    }

    public void ensureProfile(SessionUser user) {
        if (user.uid().equals(profileBootstrapUid)) {
            return;
        }
        profileBootstrapUid = user.uid();
        profileRepository.ensureProfile(user, result -> {
            if (!result.isSuccess()) {
                profileBootstrapUid = null;
            }
        });
    }

    public static final class Factory implements ViewModelProvider.Factory {
        private final AuthRepository authRepository;
        private final CloudUserProfileRepository profileRepository;

        public Factory(
                AuthRepository authRepository,
                CloudUserProfileRepository profileRepository) {
            this.authRepository = authRepository;
            this.profileRepository = profileRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.isAssignableFrom(AuthViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new AuthViewModel(authRepository, profileRepository));
        }
    }
}
