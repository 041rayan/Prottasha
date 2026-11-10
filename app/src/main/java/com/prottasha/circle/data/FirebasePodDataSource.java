package com.prottasha.circle.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.prottasha.circle.domain.PodMessage;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FirebasePodDataSource {

    public interface MessageListener {
        void onMessages(List<PodMessage> messages);
        void onError();
    }

    public interface BlocksListener {
        void onBlocksChanged(Set<String> blockedUids);
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public ListenerRegistration listen(String podId, MessageListener listener) {
        return firestore.collection("pods").document(podId).collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING).limit(50)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        listener.onError();
                        return;
                    }
                    List<PodMessage> messages = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Timestamp createdTs = document.getTimestamp("createdAt");
                        Timestamp updatedTs = document.getTimestamp("updatedAt");
                        long createdAt = createdTs == null ? 0L : createdTs.toDate().getTime();
                        long updatedAt = updatedTs == null ? createdAt : updatedTs.toDate().getTime();

                        messages.add(new PodMessage(
                                document.getId(),
                                document.getString("senderUid"),
                                document.getString("senderPseudonym"),
                                document.getString("text"),
                                createdAt,
                                updatedAt,
                                Collections.emptyMap()
                        ));
                    }
                    Collections.reverse(messages);
                    listener.onMessages(messages);
                });
    }

    public void send(String podId, String uid, String pseudonym, String text,
            ResultCallback<Boolean> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("senderUid", uid);
        data.put("senderPseudonym", pseudonym);
        data.put("text", text);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        data.put("schemaVersion", 1);

        firestore.collection("pods").document(podId).collection("messages").add(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(AppResult.success(true));
                    } else {
                        callback.onResult(AppResult.failure(task.getException() != null
                                ? task.getException()
                                : new IllegalStateException("Message rejected")));
                    }
                });
    }

    public void edit(String podId, String messageId, String newText,
            ResultCallback<Boolean> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("text", newText);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection("pods").document(podId).collection("messages").document(messageId)
                .update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(AppResult.success(true));
                    } else {
                        callback.onResult(AppResult.failure(task.getException() != null
                                ? task.getException()
                                : new IllegalStateException("Edit rejected")));
                    }
                });
    }

    public void delete(String podId, String messageId,
            ResultCallback<Boolean> callback) {
        firestore.collection("pods").document(podId).collection("messages").document(messageId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(AppResult.success(true));
                    } else {
                        callback.onResult(AppResult.failure(task.getException() != null
                                ? task.getException()
                                : new IllegalStateException("Delete rejected")));
                    }
                });
    }

    public void setReaction(String podId, String messageId, String uid, String emoji,
            ResultCallback<Boolean> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("emoji", emoji);

        firestore.collection("pods").document(podId).collection("messages").document(messageId)
                .collection("reactions").document(uid).set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(AppResult.success(true));
                    } else {
                        callback.onResult(AppResult.failure(task.getException() != null
                                ? task.getException()
                                : new IllegalStateException("Reaction rejected")));
                    }
                });
    }

    public void removeReaction(String podId, String messageId, String uid,
            ResultCallback<Boolean> callback) {
        firestore.collection("pods").document(podId).collection("messages").document(messageId)
                .collection("reactions").document(uid).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(AppResult.success(true));
                    } else {
                        callback.onResult(AppResult.failure(task.getException() != null
                                ? task.getException()
                                : new IllegalStateException("Reaction removal rejected")));
                    }
                });
    }

    public void report(String podId, String messageId, String uid, String reason,
            ResultCallback<Boolean> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("reason", reason);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("status", "OPEN");

        firestore.collection("pods").document(podId).collection("messages").document(messageId)
                .collection("reports").document(uid).set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(AppResult.success(true));
                    } else {
                        callback.onResult(AppResult.failure(task.getException() != null
                                ? task.getException()
                                : new IllegalStateException("Report rejected")));
                    }
                });
    }

    public void blockUser(String currentUid, String targetUid,
            ResultCallback<Boolean> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("blockedAt", FieldValue.serverTimestamp());

        firestore.collection("users").document(currentUid).collection("blocks").document(targetUid)
                .set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(AppResult.success(true));
                    } else {
                        callback.onResult(AppResult.failure(task.getException() != null
                                ? task.getException()
                                : new IllegalStateException("Block rejected")));
                    }
                });
    }

    public void unblockUser(String currentUid, String targetUid,
            ResultCallback<Boolean> callback) {
        firestore.collection("users").document(currentUid).collection("blocks").document(targetUid)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(AppResult.success(true));
                    } else {
                        callback.onResult(AppResult.failure(task.getException() != null
                                ? task.getException()
                                : new IllegalStateException("Unblock rejected")));
                    }
                });
    }

    public ListenerRegistration listenBlocks(String currentUid, BlocksListener listener) {
        return firestore.collection("users").document(currentUid).collection("blocks")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        return;
                    }
                    Set<String> blocked = new HashSet<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        blocked.add(doc.getId());
                    }
                    listener.onBlocksChanged(blocked);
                });
    }

    public void leavePod(String podId, String uid, ResultCallback<Boolean> callback) {
        // 1. Delete member from Firestore
        firestore.collection("pods").document(podId).collection("members").document(uid)
                .delete()
                .addOnCompleteListener(fsTask -> {
                    // 2. Delete member mirror from RTDB
                    FirebaseDatabase.getInstance().getReference("podMembers")
                            .child(podId).child(uid).removeValue();
                    if (fsTask.isSuccessful()) {
                        callback.onResult(AppResult.success(true));
                    } else {
                        callback.onResult(AppResult.failure(fsTask.getException() != null
                                ? fsTask.getException()
                                : new IllegalStateException("Leave pod failed")));
                    }
                });
    }
}
