package com.prottasha.data.model;

public class PodMessage {
    private String id;
    private String sender;
    private String text;
    private long timestamp;

    public PodMessage(String id, String sender, String text, long timestamp) {
        this.id = id;
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getSender() { return sender; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
}
