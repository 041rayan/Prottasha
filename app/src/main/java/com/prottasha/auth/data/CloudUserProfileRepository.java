package com.prottasha.auth.data;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

public class CloudUserProfileRepository {

    @Nullable
    private final FirebaseFirestore firestore;

    public CloudUserProfileRepository() {
        this(resolveFirestore());
    }

    @VisibleForTesting
    public CloudUserProfileRepository(@Nullable FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    @Nullable
    private static FirebaseFirestore resolveFirestore() {
        try {
            return FirebaseFirestore.getInstance();
        } catch (RuntimeException unavailable) {
            return null;
        }
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
            if (existing.exists() && existing.getData() != null) {
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

    public void getProfile(
            SessionUser user,
            ResultCallback<CloudUserProfile> callback) {
        if (firestore == null) {
            callback.onResult(AppResult.failure(
                    new AuthGateway.AuthFailure(AuthGateway.FailureCode.CONFIGURATION)));
            return;
        }

        DocumentReference reference = firestore.collection("users").document(user.uid());
        reference.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onResult(AppResult.failure(mapFailure(task.getException())));
                return;
            }
            com.google.firebase.firestore.DocumentSnapshot doc = task.getResult();
            if (doc != null && doc.exists() && doc.getData() != null) {
                try {
                    callback.onResult(AppResult.success(CloudUserProfile.fromFields(doc.getData())));
                } catch (RuntimeException malformed) {
                    callback.onResult(AppResult.failure(malformed));
                }
            } else {
                ensureProfile(user, callback);
            }
        });
    }

    public void updateProfile(
            SessionUser user,
            CloudUserProfile profile,
            ResultCallback<CloudUserProfile> callback) {
        if (firestore == null) {
            callback.onResult(AppResult.failure(
                    new AuthGateway.AuthFailure(AuthGateway.FailureCode.CONFIGURATION)));
            return;
        }

        if (!user.isEmailVerified()) {
            callback.onResult(AppResult.failure(
                    new AuthGateway.AuthFailure(AuthGateway.FailureCode.INVALID_CREDENTIALS)));
            return;
        }

        DocumentReference reference = firestore.collection("users").document(user.uid());
        reference.update(profile.updateFields(FieldValue.serverTimestamp()))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(AppResult.success(profile));
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
            if (code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                return new AuthGateway.AuthFailure(AuthGateway.FailureCode.INVALID_CREDENTIALS);
            }
        }
        return new AuthGateway.AuthFailure(AuthGateway.FailureCode.UNKNOWN);
    }
}
