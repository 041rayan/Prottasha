package com.prottasha.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "identities")
public class IdentityEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String identityStatement;
    private String coreValues;
    private String targetOutcome;
    private String visionImagePath;
    private long createdAt;
    private boolean isActive;

    public IdentityEntity(String identityStatement, String coreValues, String targetOutcome, String visionImagePath, long createdAt, boolean isActive) {
        this.identityStatement = identityStatement;
        this.coreValues = coreValues;
        this.targetOutcome = targetOutcome;
        this.visionImagePath = visionImagePath;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getIdentityStatement() { return identityStatement; }
    public void setIdentityStatement(String identityStatement) { this.identityStatement = identityStatement; }

    public String getCoreValues() { return coreValues; }
    public void setCoreValues(String coreValues) { this.coreValues = coreValues; }

    public String getTargetOutcome() { return targetOutcome; }
    public void setTargetOutcome(String targetOutcome) { this.targetOutcome = targetOutcome; }

    public String getVisionImagePath() { return visionImagePath; }
    public void setVisionImagePath(String visionImagePath) { this.visionImagePath = visionImagePath; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
