package com.prottasha.auth.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.prottasha.auth.domain.AuthState;
import com.prottasha.auth.domain.PasswordPolicy;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.AppResult;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Rule;
import org.junit.Test;

public final class AuthRepositoryTest {

    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private static final SessionUser UNVERIFIED =
            new SessionUser("uid-a", "Alice", "alice@example.test", false);
    private static final SessionUser VERIFIED =
            new SessionUser("uid-a", "Alice", "alice@example.test", true);

    @Test
    public void unconfiguredFirebaseFailsClosed() {
        FakeAuthGateway gateway = new FakeAuthGateway();
        gateway.configured = false;

        AuthRepository repository = new AuthRepository(gateway);

        assertState(repository, AuthState.Status.UNCONFIGURED, null);
    }

    @Test
    public void noCachedUserStartsSignedOut() {
        AuthRepository repository = new AuthRepository(new FakeAuthGateway());

        assertState(repository, AuthState.Status.SIGNED_OUT, null);
    }

    @Test
    public void cachedVerifiedUserFallsBackToPrivateOfflineModeOnNetworkFailure() {
        FakeAuthGateway gateway = new FakeAuthGateway();
        gateway.currentUser = VERIFIED;
        gateway.reloadResult = failure(AuthGateway.FailureCode.NETWORK);

        AuthRepository repository = new AuthRepository(gateway);

        assertState(repository, AuthState.Status.VERIFIED_OFFLINE, AuthState.MessageCode.OFFLINE);
        assertEquals(VERIFIED, repository.currentState().userOrNull());
    }

    @Test
    public void neverVerifiedCachedUserCannotUseOfflineMode() {
        FakeAuthGateway gateway = new FakeAuthGateway();
        gateway.currentUser = UNVERIFIED;
        gateway.reloadResult = failure(AuthGateway.FailureCode.NETWORK);

        AuthRepository repository = new AuthRepository(gateway);

        assertState(repository, AuthState.Status.UNVERIFIED, AuthState.MessageCode.NETWORK_REQUIRED);
    }

    @Test
    public void creatingAccountSendsVerificationAndRemainsLocked() {
        FakeAuthGateway gateway = new FakeAuthGateway();
        gateway.createResult = AppResult.success(UNVERIFIED);
        AuthRepository repository = new AuthRepository(gateway);

        repository.createAccount("Alice", "alice@example.test", "Strong123");

        assertEquals(1, gateway.verificationCalls);
        assertState(repository, AuthState.Status.UNVERIFIED,
                AuthState.MessageCode.VERIFICATION_SENT);
        assertEquals(UNVERIFIED, repository.currentState().userOrNull());
    }

    @Test
    public void signInReloadsAndRoutesVerifiedUserOnline() {
        FakeAuthGateway gateway = new FakeAuthGateway();
        gateway.signInResult = AppResult.success(UNVERIFIED);
        gateway.reloadResult = AppResult.success(VERIFIED);
        AuthRepository repository = new AuthRepository(gateway);

        repository.signIn("alice@example.test", "Strong123");

        assertState(repository, AuthState.Status.VERIFIED_ONLINE, null);
        assertEquals(VERIFIED, repository.currentState().userOrNull());
    }

    @Test
    public void wrongPasswordUsesStableNonEnumeratingMessage() {
        FakeAuthGateway gateway = new FakeAuthGateway();
        gateway.signInResult = failure(AuthGateway.FailureCode.INVALID_CREDENTIALS);
        AuthRepository repository = new AuthRepository(gateway);

        repository.signIn("alice@example.test", "wrong");

        assertState(repository, AuthState.Status.ERROR,
                AuthState.MessageCode.INVALID_CREDENTIALS);
        assertNull(repository.currentState().userOrNull());
    }

    @Test
    public void throttlingUsesStableRetryMessage() {
        FakeAuthGateway gateway = new FakeAuthGateway();
        gateway.signInResult = failure(AuthGateway.FailureCode.TOO_MANY_REQUESTS);
        AuthRepository repository = new AuthRepository(gateway);

        repository.signIn("alice@example.test", "Strong123");

        assertState(repository, AuthState.Status.ERROR, AuthState.MessageCode.TRY_LATER);
    }

    @Test
    public void callbackArrivingAfterSignOutCannotRestoreOldUser() {
        FakeAuthGateway gateway = new FakeAuthGateway();
        gateway.signInResult = AppResult.success(VERIFIED);
        gateway.holdSignIn = true;
        AuthRepository repository = new AuthRepository(gateway);

        repository.signIn("alice@example.test", "Strong123");
        repository.signOut();
        gateway.deliverPending();

        assertEquals(1, gateway.signOutCalls);
        assertState(repository, AuthState.Status.SIGNED_OUT, null);
    }

    @Test
    public void passwordResetConfirmationDoesNotRevealAccountExistence() {
        FakeAuthGateway gateway = new FakeAuthGateway();
        AuthRepository repository = new AuthRepository(gateway);

        repository.sendPasswordReset("unknown@example.test");

        assertEquals(1, gateway.resetCalls);
        assertState(repository, AuthState.Status.SIGNED_OUT,
                AuthState.MessageCode.RESET_EMAIL_SENT);
    }

    @Test
    public void approvedPasswordPolicyMatchesFirebaseConfiguration() {
        assertEquals(PasswordPolicy.Result.TOO_SHORT,
                PasswordPolicy.validate("Short1"));
        assertEquals(PasswordPolicy.Result.NEEDS_UPPERCASE,
                PasswordPolicy.validate("lowercase1"));
        assertEquals(PasswordPolicy.Result.NEEDS_LOWERCASE,
                PasswordPolicy.validate("UPPERCASE1"));
        assertEquals(PasswordPolicy.Result.NEEDS_NUMBER,
                PasswordPolicy.validate("NoNumbers"));
        assertEquals(PasswordPolicy.Result.VALID,
                PasswordPolicy.validate("Strong123"));
    }

    private static AppResult<SessionUser> failure(AuthGateway.FailureCode code) {
        return AppResult.failure(new AuthGateway.AuthFailure(code));
    }

    private static void assertState(
            AuthRepository repository,
            AuthState.Status status,
            AuthState.MessageCode message) {
        assertEquals(status, repository.currentState().status());
        assertEquals(message, repository.currentState().messageOrNull());
    }
}
