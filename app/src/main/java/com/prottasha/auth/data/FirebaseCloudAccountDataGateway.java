package com.prottasha.auth.data;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.WriteBatch;
import com.prottasha.circle.data.PodRepository;
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
        com.google.firebase.firestore.DocumentReference member = firestore.collection("pods")
                .document(PodRepository.SHOWCASE_POD_ID).collection("members").document(uid);
        member.get().addOnCompleteListener(membership -> {
            if (membership.isSuccessful()
                    && membership.getResult() != null
                    && membership.getResult().exists()) {
                cleanupCircle(uid, callback);
            } else if (membership.isSuccessful()) {
                deleteProfile(uid, callback);
            } else {
                callback.onResult(AppResult.failure(new AuthGateway.AuthFailure(
                        AuthGateway.FailureCode.NETWORK)));
            }
        });
    }

    private void cleanupCircle(String uid, ResultCallback<Boolean> callback) {
        firestore.collection("pods").document(PodRepository.SHOWCASE_POD_ID)
                .collection("messages").limit(100).get().addOnCompleteListener(messages -> {
            if (!messages.isSuccessful() || messages.getResult() == null) {
                callback.onResult(AppResult.failure(new AuthGateway.AuthFailure(
                        AuthGateway.FailureCode.NETWORK)));
                return;
            }
            WriteBatch batch = firestore.batch();
            messages.getResult().getDocuments().forEach(message -> {
                batch.delete(message.getReference().collection("reactions").document(uid));
                batch.delete(message.getReference().collection("reports").document(uid));
                if (uid.equals(message.getString("senderUid"))) {
                    batch.delete(message.getReference());
                }
            });
            batch.delete(firestore.collection("pods").document(PodRepository.SHOWCASE_POD_ID)
                    .collection("members").document(uid));
            batch.commit().addOnCompleteListener(cleaned -> {
                if (!cleaned.isSuccessful()) {
                    callback.onResult(AppResult.failure(new AuthGateway.AuthFailure(
                            AuthGateway.FailureCode.NETWORK)));
                    return;
                }
                FirebaseDatabase.getInstance().getReference("podMembers")
                        .child(PodRepository.SHOWCASE_POD_ID).child(uid).removeValue();
                deleteProfile(uid, callback);
            });
        });
    }

    private void deleteProfile(String uid, ResultCallback<Boolean> callback) {
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
