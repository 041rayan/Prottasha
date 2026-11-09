package com.prottasha.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "triggers")
public class TriggerEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String situationCue;
    private String underlyingMotive;
    private String ifThenPlan;
    private int urgeSurfingSeconds;
    private int timesEncountered;
    private int timesOvercome;

    public TriggerEntity(String situationCue, String underlyingMotive, String ifThenPlan, int urgeSurfingSeconds) {
        this.situationCue = situationCue;
        this.underlyingMotive = underlyingMotive;
        this.ifThenPlan = ifThenPlan;
        this.urgeSurfingSeconds = urgeSurfingSeconds;
        this.timesEncountered = 0;
        this.timesOvercome = 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSituationCue() { return situationCue; }
    public void setSituationCue(String situationCue) { this.situationCue = situationCue; }

    public String getUnderlyingMotive() { return underlyingMotive; }
    public void setUnderlyingMotive(String underlyingMotive) { this.underlyingMotive = underlyingMotive; }

    public String getIfThenPlan() { return ifThenPlan; }
    public void setIfThenPlan(String ifThenPlan) { this.ifThenPlan = ifThenPlan; }

    public int getUrgeSurfingSeconds() { return urgeSurfingSeconds; }
    public void setUrgeSurfingSeconds(int urgeSurfingSeconds) { this.urgeSurfingSeconds = urgeSurfingSeconds; }

    public int getTimesEncountered() { return timesEncountered; }
    public void setTimesEncountered(int timesEncountered) { this.timesEncountered = timesEncountered; }

    public int getTimesOvercome() { return timesOvercome; }
    public void setTimesOvercome(int timesOvercome) { this.timesOvercome = timesOvercome; }
}
