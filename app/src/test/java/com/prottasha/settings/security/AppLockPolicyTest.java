package com.prottasha.settings.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class AppLockPolicyTest {

    @Test
    public void timeoutRequiresUnlockOnlyAfterConfiguredDuration() {
        AppLockManager lock = new AppLockManager(30_000L); // 30s timeout
        long t0 = 1000L;
        lock.onBackground(t0);

        // Within timeout: no unlock required
        assertFalse(lock.requiresUnlock(t0 + 10_000L));
        assertFalse(lock.requiresUnlock(t0 + 30_000L));

        // After timeout: unlock required
        assertTrue(lock.requiresUnlock(t0 + 30_001L));

        // After unlocking: no unlock required until backgrounded again
        lock.unlock();
        assertFalse(lock.requiresUnlock(t0 + 40_000L));
    }

    @Test
    public void accountChangeForcesLockImmediately() {
        AppLockManager lock = new AppLockManager(60_000L);
        assertFalse(lock.requiresUnlock(1000L));

        lock.onAccountChanged();
        assertTrue(lock.requiresUnlock(1000L));

        lock.unlock();
        assertFalse(lock.requiresUnlock(1000L));
    }

    @Test
    public void wrongPinAttemptsTriggerLockoutCooldown() throws Exception {
        AppLockPolicy policy = new AppLockPolicy();
        PinCredentialStore.Verifier verifier = PinCredentialStore.create("1234".toCharArray());
        policy.setPinVerifier(verifier);

        long now = 100_000L;
        // 4 failed attempts should not trigger lockout
        for (int i = 0; i < 4; i++) {
            assertFalse(policy.verifyPin("0000".toCharArray(), now));
            assertFalse(policy.isLockedOut(now));
        }

        // 5th failed attempt triggers 30s lockout
        assertFalse(policy.verifyPin("0000".toCharArray(), now));
        assertTrue(policy.isLockedOut(now));
        assertTrue(policy.isLockedOut(now + 29_000L));

        // After 30s cooldown expires, lockout clears
        assertFalse(policy.isLockedOut(now + 30_001L));

        // Correct PIN succeeds and clears failed attempts
        assertTrue(policy.verifyPin("1234".toCharArray(), now + 31_000L));
        assertEquals(0, policy.getFailedAttempts());
    }

    @Test
    public void sensitiveActionGatingRequiresUnlockedState() {
        AppLockManager lock = new AppLockManager(0L); // immediate lock
        lock.onBackground(1000L);
        assertTrue(lock.requiresUnlock(1001L));

        // Gated action rejected while locked
        assertFalse(AppLockPolicy.canPerformSensitiveAction(lock, 1001L));

        lock.unlock();
        assertTrue(AppLockPolicy.canPerformSensitiveAction(lock, 1001L));
    }
}
