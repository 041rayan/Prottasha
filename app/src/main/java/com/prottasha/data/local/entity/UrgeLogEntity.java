package com.prottasha.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "urge_logs")
public class UrgeLogEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long timestamp;
    private String targetPackageOrDomain;
    private int delaySeconds;
    private boolean resisted;
    private String reflectionNote;

    public UrgeLogEntity(long timestamp, String targetPackageOrDomain, int delaySeconds, boolean resisted, String reflectionNote) {
        this.timestamp = timestamp;
        this.targetPackageOrDomain = targetPackageOrDomain;
        this.delaySeconds = delaySeconds;
        this.resisted = resisted;
        this.reflectionNote = reflectionNote;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getTargetPackageOrDomain() { return targetPackageOrDomain; }
    public void setTargetPackageOrDomain(String targetPackageOrDomain) { this.targetPackageOrDomain = targetPackageOrDomain; }

    public int getDelaySeconds() { return delaySeconds; }
    public void setDelaySeconds(int delaySeconds) { this.delaySeconds = delaySeconds; }

    public boolean isResisted() { return resisted; }
    public void setResisted(boolean resisted) { this.resisted = resisted; }

    public String getReflectionNote() { return reflectionNote; }
    public void setReflectionNote(String reflectionNote) { this.reflectionNote = reflectionNote; }
}
