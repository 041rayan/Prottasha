package com.prottasha.circle.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public final class PresenceRepository {
    private DatabaseReference reference;

    public void connect(String podId, String uid) {
        reference = FirebaseDatabase.getInstance().getReference("presence")
                .child(podId).child(uid);
        Map<String, Object> online = new HashMap<>();
        online.put("online", true);
        online.put("lastChanged", ServerValue.TIMESTAMP);
        Map<String, Object> offline = new HashMap<>();
        offline.put("online", false);
        offline.put("lastChanged", ServerValue.TIMESTAMP);
        reference.onDisconnect().setValue(offline);
        reference.setValue(online);
    }

    public void disconnect() {
        if (reference != null) reference.removeValue();
        reference = null;
    }
}
