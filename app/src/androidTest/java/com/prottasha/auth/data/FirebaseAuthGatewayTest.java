package com.prottasha.auth.data;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class FirebaseAuthGatewayTest {

    @Test
    public void mapsKnownFirebaseFailuresToStableCodes() {
        assertCode(AuthGateway.FailureCode.NETWORK,
                new FirebaseNetworkException("provider detail"));
        assertCode(AuthGateway.FailureCode.TOO_MANY_REQUESTS,
                new FirebaseTooManyRequestsException("provider detail"));
        assertCode(AuthGateway.FailureCode.INVALID_CREDENTIALS,
                new FirebaseAuthInvalidCredentialsException(
                        "ERROR_INVALID_LOGIN_CREDENTIALS", "detail"));
        assertCode(AuthGateway.FailureCode.EMAIL_IN_USE,
                new FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "detail"));
        assertCode(AuthGateway.FailureCode.WEAK_PASSWORD,
                new FirebaseAuthWeakPasswordException(
                        "ERROR_WEAK_PASSWORD", "detail", "reason"));
        assertCode(AuthGateway.FailureCode.REQUIRES_RECENT_LOGIN,
                new FirebaseAuthRecentLoginRequiredException(
                        "ERROR_REQUIRES_RECENT_LOGIN", "detail"));
        assertCode(AuthGateway.FailureCode.UNKNOWN, new IllegalStateException("secret detail"));
    }

    private static void assertCode(AuthGateway.FailureCode expected, Throwable error) {
        assertEquals(expected, FirebaseAuthGateway.mapFailure(error).code());
    }
}
