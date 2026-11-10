package com.prottasha.circle.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PodMessage {
    public final String id;
    public final String senderUid;
    public final String pseudonym;
    public final String text;
    public final long timestampMillis;
    public final long updatedAtMillis;
    public final Map<String, String> reactions; // uid -> emoji

    public PodMessage(String id, String senderUid, String pseudonym,
            String text, long timestampMillis) {
        this(id, senderUid, pseudonym, text, timestampMillis, timestampMillis, Collections.emptyMap());
    }

    public PodMessage(
            @NonNull String id,
            @NonNull String senderUid,
            @NonNull String pseudonym,
            @NonNull String text,
            long timestampMillis,
            long updatedAtMillis,
            @Nullable Map<String, String> reactions) {
        this.id = id;
        this.senderUid = senderUid;
        this.pseudonym = pseudonym;
        this.text = text;
        this.timestampMillis = timestampMillis;
        this.updatedAtMillis = updatedAtMillis;
        this.reactions = reactions != null ? Collections.unmodifiableMap(new HashMap<>(reactions)) : Collections.emptyMap();
    }

    public boolean isEdited() {
        return updatedAtMillis > timestampMillis;
    }

    public Map<String, Integer> reactionCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (String emoji : reactions.values()) {
            counts.put(emoji, counts.getOrDefault(emoji, 0) + 1);
        }
        return counts;
    }
}
