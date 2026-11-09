package com.prottasha.circle.data;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.prottasha.circle.domain.PodMessage;
import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FirebasePodDataSource {
    public interface MessageListener { void onMessages(List<PodMessage> messages); void onError(); }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public ListenerRegistration listen(String podId, MessageListener listener) {
        return firestore.collection("pods").document(podId).collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING).limit(50)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) { listener.onError(); return; }
                    List<PodMessage> messages = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Timestamp timestamp = document.getTimestamp("createdAt");
                        messages.add(new PodMessage(document.getId(),
                                document.getString("senderUid"),
                                document.getString("senderPseudonym"),
                                document.getString("text"),
                                timestamp == null ? 0L : timestamp.toDate().getTime()));
                    }
                    java.util.Collections.reverse(messages);
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
                .addOnCompleteListener(task -> callback.onResult(task.isSuccessful()
                        ? AppResult.success(true)
                        : AppResult.failure(new IllegalStateException("Message rejected"))));
    }
}
