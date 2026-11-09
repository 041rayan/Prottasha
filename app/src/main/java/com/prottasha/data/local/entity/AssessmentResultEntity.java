package com.prottasha.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "assessment_results")
public class AssessmentResultEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String instrumentType;
    private int totalScore;
    private String riskBand;
    private boolean triggersReferral;
    private long timestamp;

    public AssessmentResultEntity(String instrumentType, int totalScore, String riskBand, boolean triggersReferral, long timestamp) {
        this.instrumentType = instrumentType;
        this.totalScore = totalScore;
        this.riskBand = riskBand;
        this.triggersReferral = triggersReferral;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getInstrumentType() { return instrumentType; }
    public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getRiskBand() { return riskBand; }
    public void setRiskBand(String riskBand) { this.riskBand = riskBand; }

    public boolean isTriggersReferral() { return triggersReferral; }
    public void setTriggersReferral(boolean triggersReferral) { this.triggersReferral = triggersReferral; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
