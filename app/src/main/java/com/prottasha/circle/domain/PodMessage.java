package com.prottasha.circle.domain;

public final class PodMessage {
    public final String id;
    public final String senderUid;
    public final String pseudonym;
    public final String text;
    public final long timestampMillis;

    public PodMessage(String id, String senderUid, String pseudonym,
            String text, long timestampMillis) {
        this.id = id;
        this.senderUid = senderUid;
        this.pseudonym = pseudonym;
        this.text = text;
        this.timestampMillis = timestampMillis;
    }
}
