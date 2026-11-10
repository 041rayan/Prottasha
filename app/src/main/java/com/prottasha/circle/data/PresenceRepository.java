package com.prottasha.circle.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public final class PresenceRepository {

    public interface PresenceListener {
        void onPresenceChanged(int onlineCount, Map<String, Boolean> presenceMap);
    }

    private DatabaseReference reference;
    private ValueEventListener connectedListener;

    public void connect(String podId, String uid) {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        reference = FirebaseDatabase.getInstance().getReference("presence")
                .child(podId).child(uid);

        Map<String, Object> offline = new HashMap<>();
        offline.put("online", false);
        offline.put("lastChanged", ServerValue.TIMESTAMP);

        Map<String, Object> online = new HashMap<>();
        online.put("online", true);
        online.put("lastChanged", ServerValue.TIMESTAMP);

        connectedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(connected) && reference != null) {
                    reference.onDisconnect().setValue(offline);
                    reference.setValue(online);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        connectedRef.addValueEventListener(connectedListener);
    }

    public void disconnect() {
        if (connectedListener != null) {
            FirebaseDatabase.getInstance().getReference(".info/connected").removeEventListener(connectedListener);
            connectedListener = null;
        }
        if (reference != null) {
            Map<String, Object> offline = new HashMap<>();
            offline.put("online", false);
            offline.put("lastChanged", ServerValue.TIMESTAMP);
            reference.setValue(offline);
            reference = null;
        }
    }

    public ValueEventListener listenPresence(@NonNull String podId, @NonNull PresenceListener listener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("presence").child(podId);
        ValueEventListener valueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                Map<String, Boolean> map = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.getKey();
                    if (uid == null) continue;
                    Boolean online = child.child("online").getValue(Boolean.class);
                    boolean isOnline = Boolean.TRUE.equals(online);
                    map.put(uid, isOnline);
                    if (isOnline) {
                        count++;
                    }
                }
                listener.onPresenceChanged(count, map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        ref.addValueEventListener(valueListener);
        return valueListener;
    }

    public void removeListener(@NonNull String podId, @Nullable ValueEventListener listener) {
        if (listener != null) {
            FirebaseDatabase.getInstance().getReference("presence").child(podId).removeEventListener(listener);
        }
    }
}
