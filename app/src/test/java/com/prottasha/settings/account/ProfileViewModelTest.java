package com.prottasha.settings.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.prottasha.auth.data.AuthGateway;
import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.data.CloudUserProfile;
import com.prottasha.auth.data.CloudUserProfileRepository;
import com.prottasha.auth.data.FakeAuthGateway;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.auth.domain.UserSessionManager;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;
import com.prottasha.core.security.EncryptedPrivateFileStore;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ProfileViewModelTest {

    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private static final SessionUser USER =
            new SessionUser("uid-profile-test", "Alice", "alice@test.local", true);

    @Test
    public void loadsProfileFromRepositoryOnStart() {
        FakeProfileRepository profileRepo = new FakeProfileRepository();
        profileRepo.profileResult = AppResult.success(
                CloudUserProfile.bootstrap(USER)
                        .withAvatar("seed-1", "HOPE_EMERALD", "INITIALS"));

        FakeAuthGateway authGateway = new FakeAuthGateway();
        authGateway.currentUser = USER;
        authGateway.reloadResult = AppResult.success(USER);
        AuthRepository authRepo = new AuthRepository(authGateway);

        ProfileViewModel viewModel = new ProfileViewModel(
                USER, authRepo, profileRepo, null);

        viewModel.loadProfile();

        CloudUserProfile loaded = viewModel.profile().getValue();
        assertNotNull(loaded);
        assertEquals("Alice", loaded.displayName());
        assertEquals("HOPE_EMERALD", loaded.avatarPaletteOrNull());
        assertEquals("INITIALS", loaded.avatarStyleOrNull());
    }

    @Test
    public void unconfiguredOrOfflineProfileLoadsGracefully() {
        FakeProfileRepository profileRepo = new FakeProfileRepository();
        profileRepo.profileResult = AppResult.failure(
                new AuthGateway.AuthFailure(AuthGateway.FailureCode.CONFIGURATION));

        FakeAuthGateway authGateway = new FakeAuthGateway();
        authGateway.currentUser = USER;
        authGateway.reloadResult = AppResult.success(USER);
        AuthRepository authRepo = new AuthRepository(authGateway);

        ProfileViewModel viewModel = new ProfileViewModel(
                USER, authRepo, profileRepo, null);

        viewModel.loadProfile();

        assertNotNull(viewModel.errorMessage().getValue());
    }

    @Test
    public void savingProfileUpdatesBothCloudAndAuthDisplayName() {
        FakeProfileRepository profileRepo = new FakeProfileRepository();
        CloudUserProfile initial = CloudUserProfile.bootstrap(USER);
        profileRepo.profileResult = AppResult.success(initial);
        profileRepo.updateResult = AppResult.success(initial.withDisplayName("Alice Renamed"));

        FakeAuthGateway authGateway = new FakeAuthGateway();
        authGateway.currentUser = USER;
        authGateway.reloadResult = AppResult.success(USER);
        authGateway.updateDisplayNameResult = AppResult.success(
                new SessionUser(USER.uid(), "Alice Renamed", USER.email(), true));
        AuthRepository authRepo = new AuthRepository(authGateway);

        ProfileViewModel viewModel = new ProfileViewModel(
                USER, authRepo, profileRepo, null);

        viewModel.loadProfile();
        viewModel.saveProfile("Alice Renamed", "Hope-Custom", "HOPE_EMERALD", "GEOMETRIC");

        assertEquals(1, profileRepo.updateCalls);
        assertEquals(1, authGateway.updateDisplayNameCalls);
        assertTrue(viewModel.saveSuccess().getValue());
    }

    private static class FakeProfileRepository extends CloudUserProfileRepository {
        AppResult<CloudUserProfile> profileResult;
        AppResult<CloudUserProfile> updateResult;
        int updateCalls = 0;

        FakeProfileRepository() {
            super(null);
        }

        @Override
        public void getProfile(SessionUser user, ResultCallback<CloudUserProfile> callback) {
            callback.onResult(profileResult);
        }

        @Override
        public void updateProfile(SessionUser user, CloudUserProfile profile, ResultCallback<CloudUserProfile> callback) {
            updateCalls++;
            callback.onResult(updateResult != null ? updateResult : AppResult.success(profile));
        }
    }
}
