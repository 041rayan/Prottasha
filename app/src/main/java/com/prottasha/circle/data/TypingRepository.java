package com.prottasha.circle.data;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TypingRepository {

    public static final long TYPING_DEBOUNCE_MILLIS = 1500L;
    public static final long STALE_TYPING_THRESHOLD_MILLIS = 8000L;

    public interface TypingListener {
        void onTypingChanged(List<String> typingUids);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable stopTypingRunnable;
    private boolean isCurrentlyTyping = false;

    public static boolean isTypingActive(boolean typing, long lastChanged, long now) {
        if (!typing) return false;
        long elapsed = now - lastChanged;
        return elapsed >= -5000L && elapsed <= STALE_TYPING_THRESHOLD_MILLIS;
    }

    public void onUserKeyTyped(@NonNull String podId, @NonNull String uid) {
        if (stopTypingRunnable != null) {
            handler.removeCallbacks(stopTypingRunnable);
        }

        if (!isCurrentlyTyping) {
            isCurrentlyTyping = true;
            setTyping(podId, uid, true);
        }

        stopTypingRunnable = () -> stopTyping(podId, uid);
        handler.postDelayed(stopTypingRunnable, TYPING_DEBOUNCE_MILLIS);
    }

    public void stopTyping(@NonNull String podId, @NonNull String uid) {
        if (stopTypingRunnable != null) {
            handler.removeCallbacks(stopTypingRunnable);
            stopTypingRunnable = null;
        }
        if (isCurrentlyTyping) {
            isCurrentlyTyping = false;
            setTyping(podId, uid, false);
        }
    }

    public void setTyping(@NonNull String podId, @NonNull String uid, boolean typing) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("typing")
                .child(podId).child(uid);

        Map<String, Object> data = new HashMap<>();
        data.put("typing", typing);
        data.put("lastChanged", ServerValue.TIMESTAMP);

        Map<String, Object> disconnectData = new HashMap<>();
        disconnectData.put("typing", false);
        disconnectData.put("lastChanged", ServerValue.TIMESTAMP);

        ref.onDisconnect().setValue(disconnectData);
        ref.setValue(data);
    }

    public ValueEventListener listenTyping(
            @NonNull String podId,
            @Nullable String myUid,
            @NonNull TypingListener listener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("typing").child(podId);
        ValueEventListener valueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long now = System.currentTimeMillis();
                List<String> typingUids = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.getKey();
                    if (uid == null || (myUid != null && uid.equals(myUid))) {
                        continue;
                    }
                    Boolean typing = child.child("typing").getValue(Boolean.class);
                    Long lastChanged = child.child("lastChanged").getValue(Long.class);
                    if (typing != null && lastChanged != null) {
                        if (isTypingActive(typing, lastChanged, now)) {
                            typingUids.add(uid);
                        }
                    }
                }
                listener.onTypingChanged(typingUids);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Denied or cancelled
            }
        };
        ref.addValueEventListener(valueListener);
        return valueListener;
    }

    public void removeListener(@NonNull String podId, @Nullable ValueEventListener listener) {
        if (listener != null) {
            FirebaseDatabase.getInstance().getReference("typing").child(podId).removeEventListener(listener);
        }
    }
}
