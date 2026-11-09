package com.prottasha.auth.data;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

public final class CloudUserProfileRepository {

    @Nullable
    private final FirebaseFirestore firestore;

    public CloudUserProfileRepository() {
        FirebaseFirestore configured;
        try {
            configured = FirebaseFirestore.getInstance();
        } catch (RuntimeException unavailable) {
            configured = null;
        }
        firestore = configured;
    }

    public boolean isConfigured() {
        return firestore != null;
    }

    public void ensureProfile(
            SessionUser user,
            ResultCallback<CloudUserProfile> callback) {
        if (firestore == null) {
            callback.onResult(AppResult.failure(
                    new AuthGateway.AuthFailure(AuthGateway.FailureCode.CONFIGURATION)));
            return;
        }

        DocumentReference reference = firestore.collection("users").document(user.uid());
        firestore.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot existing = transaction.get(reference);
            if (existing.exists()) {
                return CloudUserProfile.fromFields(existing.getData());
            }
            CloudUserProfile profile = CloudUserProfile.bootstrap(user);
            transaction.set(reference, profile.createFields(FieldValue.serverTimestamp()));
            return profile;
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                callback.onResult(AppResult.success(task.getResult()));
            } else {
                callback.onResult(AppResult.failure(mapFailure(task.getException())));
            }
        });
    }

    private static AuthGateway.AuthFailure mapFailure(@Nullable Throwable error) {
        if (error instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException.Code code = ((FirebaseFirestoreException) error).getCode();
            if (code == FirebaseFirestoreException.Code.UNAVAILABLE
                    || code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED) {
                return new AuthGateway.AuthFailure(AuthGateway.FailureCode.NETWORK);
            }
        }
        return new AuthGateway.AuthFailure(AuthGateway.FailureCode.UNKNOWN);
    }
}
