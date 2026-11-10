package com.prottasha.settings.account;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.data.CloudUserProfile;
import com.prottasha.auth.data.CloudUserProfileRepository;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.security.EncryptedPrivateFileStore;
import com.prottasha.core.security.StoredPrivateImage;
import com.prottasha.core.security.UserDatabaseIdentity;

import java.io.InputStream;
import java.util.List;

public final class ProfileViewModel extends ViewModel {

    private final SessionUser user;
    private final AuthRepository authRepository;
    private final CloudUserProfileRepository profileRepository;
    @Nullable
    private final EncryptedPrivateFileStore fileStore;

    private final MutableLiveData<CloudUserProfile> profileState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccessState = new MutableLiveData<>();
    private final MutableLiveData<byte[]> localPhotoState = new MutableLiveData<>();

    public ProfileViewModel(
            SessionUser user,
            AuthRepository authRepository,
            CloudUserProfileRepository profileRepository,
            @Nullable EncryptedPrivateFileStore fileStore) {
        this.user = user;
        this.authRepository = authRepository;
        this.profileRepository = profileRepository;
        this.fileStore = fileStore;
    }

    public LiveData<CloudUserProfile> profile() {
        return profileState;
    }

    public LiveData<Boolean> isLoading() {
        return loadingState;
    }

    public LiveData<String> errorMessage() {
        return errorState;
    }

    public LiveData<Boolean> saveSuccess() {
        return saveSuccessState;
    }

    public LiveData<byte[]> localPhoto() {
        return localPhotoState;
    }

    public void loadProfile() {
        loadingState.setValue(true);
        errorState.setValue(null);
        profileRepository.getProfile(user, result -> {
            loadingState.setValue(false);
            if (result.isSuccess() && result.valueOrNull() != null) {
                profileState.setValue(result.valueOrNull());
            } else {
                errorState.setValue("Unable to load cloud profile");
            }
        });
        loadLocalPhoto();
    }

    private void loadLocalPhoto() {
        if (fileStore == null) {
            return;
        }
        try {
            UserDatabaseIdentity identity = UserDatabaseIdentity.fromUid(user.uid());
            List<String> images = fileStore.listStorageIds(identity, EncryptedPrivateFileStore.Category.PROFILE);
            if (!images.isEmpty()) {
                byte[] bytes = fileStore.readImage(
                        identity, EncryptedPrivateFileStore.Category.PROFILE, images.get(0));
                localPhotoState.postValue(bytes);
            } else {
                localPhotoState.postValue(null);
            }
        } catch (Exception ignored) {
            localPhotoState.postValue(null);
        }
    }

    public void saveProfile(
            String displayName,
            String pseudonym,
            String avatarPalette,
            String avatarStyle) {
        if (displayName == null || displayName.trim().isEmpty()) {
            errorState.setValue("Display name cannot be empty");
            return;
        }
        if (pseudonym == null || pseudonym.trim().isEmpty()) {
            errorState.setValue("Pseudonym cannot be empty");
            return;
        }

        loadingState.setValue(true);
        errorState.setValue(null);
        saveSuccessState.setValue(false);

        String trimmedName = displayName.trim();
        String trimmedPseudonym = pseudonym.trim();

        CloudUserProfile current = profileState.getValue();
        String seed = (current != null && current.avatarSeedOrNull() != null)
                ? current.avatarSeedOrNull()
                : UserDatabaseIdentity.fromUid(user.uid()).uidHash().substring(0, 8);

        CloudUserProfile updated = (current != null ? current : CloudUserProfile.bootstrap(user))
                .withDisplayName(trimmedName)
                .withPodPseudonym(trimmedPseudonym)
                .withAvatar(seed, avatarPalette, avatarStyle);

        profileRepository.updateProfile(user, updated, result -> {
            if (!result.isSuccess()) {
                loadingState.setValue(false);
                errorState.setValue("Failed to update profile: "
                        + (result.errorOrNull() != null ? result.errorOrNull().getMessage() : "Unknown error"));
                return;
            }

            authRepository.updateDisplayName(trimmedName, authResult -> {
                loadingState.setValue(false);
                if (authResult.isSuccess()) {
                    profileState.setValue(updated);
                    saveSuccessState.setValue(true);
                } else {
                    errorState.setValue("Profile updated, but display name sync failed");
                    profileState.setValue(updated);
                    saveSuccessState.setValue(true);
                }
            });
        });
    }

    public void setLocalPhoto(InputStream stream) {
        if (fileStore == null) {
            return;
        }
        try {
            UserDatabaseIdentity identity = UserDatabaseIdentity.fromUid(user.uid());
            List<String> existing = fileStore.listStorageIds(
                    identity, EncryptedPrivateFileStore.Category.PROFILE);
            for (String oldId : existing) {
                fileStore.delete(identity, EncryptedPrivateFileStore.Category.PROFILE, oldId);
            }
            StoredPrivateImage stored = fileStore.storeImage(
                    identity, EncryptedPrivateFileStore.Category.PROFILE, stream);
            byte[] bytes = fileStore.readImage(
                    identity, EncryptedPrivateFileStore.Category.PROFILE, stored.storageId());
            localPhotoState.setValue(bytes);
        } catch (Exception e) {
            errorState.setValue("Failed to store private photo: " + e.getMessage());
        }
    }

    public void removeLocalPhoto() {
        if (fileStore == null) {
            return;
        }
        try {
            UserDatabaseIdentity identity = UserDatabaseIdentity.fromUid(user.uid());
            List<String> existing = fileStore.listStorageIds(
                    identity, EncryptedPrivateFileStore.Category.PROFILE);
            for (String oldId : existing) {
                fileStore.delete(identity, EncryptedPrivateFileStore.Category.PROFILE, oldId);
            }
            localPhotoState.setValue(null);
        } catch (Exception e) {
            errorState.setValue("Failed to remove private photo: " + e.getMessage());
        }
    }

    public static final class Factory implements ViewModelProvider.Factory {
        private final SessionUser user;
        private final AuthRepository authRepository;
        private final CloudUserProfileRepository profileRepository;
        private final EncryptedPrivateFileStore fileStore;

        public Factory(
                SessionUser user,
                AuthRepository authRepository,
                CloudUserProfileRepository profileRepository,
                EncryptedPrivateFileStore fileStore) {
            this.user = user;
            this.authRepository = authRepository;
            this.profileRepository = profileRepository;
            this.fileStore = fileStore;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.isAssignableFrom(ProfileViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new ProfileViewModel(
                    user, authRepository, profileRepository, fileStore));
        }
    }
}
