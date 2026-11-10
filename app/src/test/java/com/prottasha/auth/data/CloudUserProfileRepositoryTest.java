package com.prottasha.auth.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.AppResult;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public final class CloudUserProfileRepositoryTest {

    @Test
    public void unconfiguredFirestoreFailsClosed() {
        CloudUserProfileRepository repository = new CloudUserProfileRepository(null);
        assertFalse(repository.isConfigured());

        SessionUser user = new SessionUser("uid-unconfigured", "Name", "user@test.local", true);
        AtomicReference<AppResult<CloudUserProfile>> ensureRef = new AtomicReference<>();
        repository.ensureProfile(user, ensureRef::set);
        assertFalse(ensureRef.get().isSuccess());
        assertTrue(ensureRef.get().errorOrNull() instanceof AuthGateway.AuthFailure);
        assertEquals(AuthGateway.FailureCode.CONFIGURATION,
                ((AuthGateway.AuthFailure) ensureRef.get().errorOrNull()).code());

        AtomicReference<AppResult<CloudUserProfile>> getRef = new AtomicReference<>();
        repository.getProfile(user, getRef::set);
        assertFalse(getRef.get().isSuccess());
        assertEquals(AuthGateway.FailureCode.CONFIGURATION,
                ((AuthGateway.AuthFailure) getRef.get().errorOrNull()).code());

        CloudUserProfile updated = CloudUserProfile.bootstrap(user).withDisplayName("New Name");
        AtomicReference<AppResult<CloudUserProfile>> updateRef = new AtomicReference<>();
        repository.updateProfile(user, updated, updateRef::set);
        assertFalse(updateRef.get().isSuccess());
        assertEquals(AuthGateway.FailureCode.CONFIGURATION,
                ((AuthGateway.AuthFailure) updateRef.get().errorOrNull()).code());
    }

    @Test
    public void updateProfileRejectsUnverifiedEmailBeforeNetwork() {
        CloudUserProfileRepository repository = new CloudUserProfileRepository(null);
        SessionUser unverified = new SessionUser("uid-unverified", "Name", "u@test.local", false);
        CloudUserProfile profile = CloudUserProfile.bootstrap(unverified);

        AtomicReference<AppResult<CloudUserProfile>> updateRef = new AtomicReference<>();
        repository.updateProfile(unverified, profile, updateRef::set);
        assertFalse(updateRef.get().isSuccess());
    }
}
