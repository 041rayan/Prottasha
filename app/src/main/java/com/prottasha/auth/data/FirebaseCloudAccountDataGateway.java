package com.prottasha.auth.data;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

public final class FirebaseCloudAccountDataGateway implements CloudAccountDataGateway {

    @Nullable
    private final FirebaseFirestore firestore;

    public FirebaseCloudAccountDataGateway() {
        FirebaseFirestore configured;
        try {
            configured = FirebaseFirestore.getInstance();
        } catch (RuntimeException unavailable) {
            configured = null;
        }
        firestore = configured;
    }

    @Override
    public void deleteOwnedData(String uid, ResultCallback<Boolean> callback) {
        if (firestore == null) {
            callback.onResult(AppResult.failure(
                    new AuthGateway.AuthFailure(AuthGateway.FailureCode.CONFIGURATION)));
            return;
        }
        // Task 13 expands this gateway with paginated Circle-owned records.
        firestore.collection("users").document(uid).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onResult(AppResult.success(true));
            } else if (task.getException() instanceof FirebaseNetworkException) {
                callback.onResult(AppResult.failure(
                        new AuthGateway.AuthFailure(AuthGateway.FailureCode.NETWORK)));
            } else {
                callback.onResult(AppResult.failure(
                        new AuthGateway.AuthFailure(AuthGateway.FailureCode.UNKNOWN)));
            }
        });
    }
}
