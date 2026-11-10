package com.prottasha.settings.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.prottasha.auth.data.AuthGateway;
import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.data.FakeAuthGateway;
import com.prottasha.auth.domain.AccountDeletionCoordinator;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.auth.domain.UserSessionManager;
import com.prottasha.core.result.AppResult;
import com.prottasha.settings.privacy.LocalDeletionService;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class AccountViewModelTest {

    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private static final SessionUser USER =
            new SessionUser("uid-account-test", "Alice", "alice@test.local", true);

    @Test
    public void signOutClosesSessionAndSignsOut() {
        FakeAuthGateway authGateway = new FakeAuthGateway();
        authGateway.currentUser = USER;
        authGateway.reloadResult = AppResult.success(USER);
        AuthRepository authRepo = new AuthRepository(authGateway);

        List<String> events = new ArrayList<>();
        UserSessionManager.DatabaseLifecycle dbLifecycle = new UserSessionManager.DatabaseLifecycle() {
            @Override
            public void open(SessionUser user) {
                events.add("open:" + user.uid());
            }

            @Override
            public void close() {
                events.add("close");
            }

            @Override
            public void clearVerifiedBackgroundOwner() {
                events.add("clear-owner");
            }
        };

        UserSessionManager sessionManager = new UserSessionManager(
                dbLifecycle, () -> {
                    events.add("firebase-sign-out");
                    authRepo.signOut();
                });
        try {
            sessionManager.switchTo(USER);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        events.clear();

        AccountViewModel viewModel = new AccountViewModel(
                sessionManager, authRepo, null, null);

        viewModel.signOut();

        assertEquals(1, authGateway.signOutCalls);
        assertEquals(java.util.Arrays.asList("close", "clear-owner", "firebase-sign-out"), events);
        assertFalse(viewModel.hasAccountSession());
    }

    @Test
    public void signOutAndRemoveLocalDataClosesSessionAndDeletesLocalData() throws Exception {
        FakeAuthGateway authGateway = new FakeAuthGateway();
        authGateway.currentUser = USER;
        authGateway.reloadResult = AppResult.success(USER);
        AuthRepository authRepo = new AuthRepository(authGateway);

        List<String> events = new ArrayList<>();
        UserSessionManager.DatabaseLifecycle dbLifecycle = new UserSessionManager.DatabaseLifecycle() {
            @Override
            public void open(SessionUser user) {}

            @Override
            public void close() {
                events.add("close");
            }

            @Override
            public void clearVerifiedBackgroundOwner() {}
        };

        UserSessionManager sessionManager = new UserSessionManager(
                dbLifecycle, () -> {
                    events.add("firebase-sign-out");
                    authRepo.signOut();
                });
        sessionManager.switchTo(USER);

        LocalDeletionService deletionService = new LocalDeletionService(null, null) {
            @Override
            public void delete(String uidHash) {
                events.add("delete-local:" + uidHash);
            }
        };

        AccountViewModel viewModel = new AccountViewModel(
                sessionManager, authRepo, null, deletionService);

        AtomicReference<AppResult<Boolean>> resultRef = new AtomicReference<>();
        viewModel.signOutAndRemoveLocalData(resultRef::set);

        assertTrue(resultRef.get().isSuccess());
        assertTrue(events.contains("close"));
        assertTrue(events.stream().anyMatch(e -> e.startsWith("delete-local:")));
    }

    @Test
    public void changePasswordRequiresReauthenticationFirst() {
        FakeAuthGateway authGateway = new FakeAuthGateway();
        authGateway.currentUser = USER;
        authGateway.reloadResult = AppResult.success(USER);
        authGateway.events = new ArrayList<>();
        authGateway.reauthenticationResult = AppResult.failure(
                new AuthGateway.AuthFailure(AuthGateway.FailureCode.INVALID_CREDENTIALS));
        AuthRepository authRepo = new AuthRepository(authGateway);

        UserSessionManager.DatabaseLifecycle dbLifecycle = new UserSessionManager.DatabaseLifecycle() {
            @Override public void open(SessionUser user) {}
            @Override public void close() {}
            @Override public void clearVerifiedBackgroundOwner() {}
        };
        UserSessionManager sessionManager = new UserSessionManager(dbLifecycle, () -> {});
        try { sessionManager.switchTo(USER); } catch (Exception ignored) {}

        AccountViewModel viewModel = new AccountViewModel(
                sessionManager, authRepo, null, null);

        AtomicReference<AppResult<Boolean>> resultRef = new AtomicReference<>();
        viewModel.changePassword("wrongPass", "NewStrongPass123", resultRef::set);

        assertFalse(resultRef.get().isSuccess());
        assertEquals(AuthGateway.FailureCode.INVALID_CREDENTIALS,
                ((AuthGateway.AuthFailure) resultRef.get().errorOrNull()).code());
        assertEquals(0, authGateway.updatePasswordCalls);
    }
}
