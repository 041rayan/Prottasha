package com.prottasha.auth.data;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.prottasha.circle.data.PodRepository;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

import java.util.List;

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
        DocumentReference member = firestore.collection("pods")
                .document(PodRepository.SHOWCASE_POD_ID).collection("members").document(uid);
        member.get().addOnCompleteListener(membership -> {
            if (membership.isSuccessful()
                    && membership.getResult() != null
                    && membership.getResult().exists()) {
                cleanupCirclePaginated(uid, null, callback);
            } else if (membership.isSuccessful()) {
                deleteUserBlocksAndProfile(uid, callback);
            } else {
                callback.onResult(AppResult.failure(new AuthGateway.AuthFailure(
                        AuthGateway.FailureCode.NETWORK)));
            }
        });
    }

    private void cleanupCirclePaginated(
            String uid,
            @Nullable DocumentSnapshot lastSnapshot,
            ResultCallback<Boolean> callback) {

        Query query = firestore.collection("pods").document(PodRepository.SHOWCASE_POD_ID)
                .collection("messages").orderBy("createdAt").limit(50);

        if (lastSnapshot != null) {
            query = query.startAfter(lastSnapshot);
        }

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                callback.onResult(AppResult.failure(new AuthGateway.AuthFailure(
                        AuthGateway.FailureCode.NETWORK)));
                return;
            }

            QuerySnapshot snapshot = task.getResult();
            List<DocumentSnapshot> docs = snapshot.getDocuments();

            if (docs.isEmpty()) {
                // All messages paginated! Now remove membership and proceed to profile deletion
                finishCircleCleanup(uid, callback);
                return;
            }

            WriteBatch batch = firestore.batch();
            for (DocumentSnapshot message : docs) {
                batch.delete(message.getReference().collection("reactions").document(uid));
                batch.delete(message.getReference().collection("reports").document(uid));
                if (uid.equals(message.getString("senderUid"))) {
                    batch.delete(message.getReference());
                }
            }

            batch.commit().addOnCompleteListener(cleaned -> {
                if (!cleaned.isSuccessful()) {
                    callback.onResult(AppResult.failure(new AuthGateway.AuthFailure(
                            AuthGateway.FailureCode.NETWORK)));
                    return;
                }
                DocumentSnapshot nextCursor = docs.get(docs.size() - 1);
                cleanupCirclePaginated(uid, nextCursor, callback);
            });
        });
    }

    private void finishCircleCleanup(String uid, ResultCallback<Boolean> callback) {
        // Delete pod membership in Firestore
        firestore.collection("pods").document(PodRepository.SHOWCASE_POD_ID)
                .collection("members").document(uid).delete()
                .addOnCompleteListener(task -> {
                    // Mirror removal in Realtime Database
                    FirebaseDatabase.getInstance().getReference("podMembers")
                            .child(PodRepository.SHOWCASE_POD_ID).child(uid).removeValue();
                    FirebaseDatabase.getInstance().getReference("presence")
                            .child(PodRepository.SHOWCASE_POD_ID).child(uid).removeValue();
                    FirebaseDatabase.getInstance().getReference("typing")
                            .child(PodRepository.SHOWCASE_POD_ID).child(uid).removeValue();

                    deleteUserBlocksAndProfile(uid, callback);
                });
    }

    private void deleteUserBlocksAndProfile(String uid, ResultCallback<Boolean> callback) {
        // Paginate/delete blocks subcollection
        firestore.collection("users").document(uid).collection("blocks").get()
                .addOnCompleteListener(blocksTask -> {
                    if (blocksTask.isSuccessful() && blocksTask.getResult() != null && !blocksTask.getResult().isEmpty()) {
                        WriteBatch batch = firestore.batch();
                        for (DocumentSnapshot doc : blocksTask.getResult().getDocuments()) {
                            batch.delete(doc.getReference());
                        }
                        batch.commit().addOnCompleteListener(ignored -> deleteProfile(uid, callback));
                    } else {
                        deleteProfile(uid, callback);
                    }
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
