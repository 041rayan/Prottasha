package com.prottasha.data.firebase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.prottasha.data.model.PodMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebasePodManager {

    public interface OnMessagesLoadedListener {
        void onMessagesLoaded(List<PodMessage> messages);
    }

    private FirebaseFirestore db;
    private final List<PodMessage> fallbackCache = new ArrayList<>();

    public FirebasePodManager() {
        try {
            this.db = FirebaseFirestore.getInstance();
        } catch (Exception ignored) {
            this.db = null;
        }

        // Seed initial welcoming peer encouragement (AH-10, EX-15)
        fallbackCache.add(new PodMessage("1", "Marcus-10", "Starting 10-second pause mode today. Day 3 clean!", System.currentTimeMillis() - 3600000));
        fallbackCache.add(new PodMessage("2", "Scholar-99", "Cast 8 votes for my target identity today. Keep going!", System.currentTimeMillis() - 1800000));
    }

    public void listenForMessages(OnMessagesLoadedListener listener) {
        if (db == null) {
            if (listener != null) listener.onMessagesLoaded(new ArrayList<>(fallbackCache));
            return;
        }

        try {
            db.collection("pods").document("omega_pod").collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null || snapshots == null) {
                            if (listener != null) listener.onMessagesLoaded(new ArrayList<>(fallbackCache));
                            return;
                        }
                        List<PodMessage> list = new ArrayList<>();
                        snapshots.forEach(doc -> {
                            list.add(new PodMessage(
                                    doc.getId(),
                                    doc.getString("sender"),
                                    doc.getString("text"),
                                    doc.getLong("timestamp") != null ? doc.getLong("timestamp") : System.currentTimeMillis()
                            ));
                        });
                        if (listener != null) listener.onMessagesLoaded(list);
                    });
        } catch (Exception e) {
            if (listener != null) listener.onMessagesLoaded(new ArrayList<>(fallbackCache));
        }
    }

    public void sendMessage(String sender, String text) {
        PodMessage msg = new PodMessage(String.valueOf(System.currentTimeMillis()), sender, text, System.currentTimeMillis());
        fallbackCache.add(msg);

        if (db != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("sender", sender);
                data.put("text", text);
                data.put("timestamp", System.currentTimeMillis());
                db.collection("pods").document("omega_pod").collection("messages").add(data);
            } catch (Exception ignored) {}
        }
    }
}
