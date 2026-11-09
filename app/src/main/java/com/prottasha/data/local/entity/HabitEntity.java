package com.prottasha.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
    tableName = "habits",
    foreignKeys = @ForeignKey(
        entity = IdentityEntity.class,
        parentColumns = "id",
        childColumns = "identityId",
        onDelete = CASCADE
    ),
    indices = {@Index("identityId")}
)
public class HabitEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long identityId;
    private String title;
    private boolean isGoodHabit;
    private String cue;
    private String craving;
    private String response;
    private String reward;
    private int votesFor;
    private int votesAgainst;
    private int currentStreak;
    private int consecutiveMisses;
    private long lastLoggedTimestamp;

    public HabitEntity(long identityId, String title, boolean isGoodHabit, String cue, String craving, String response, String reward) {
        this.identityId = identityId;
        this.title = title;
        this.isGoodHabit = isGoodHabit;
        this.cue = cue;
        this.craving = craving;
        this.response = response;
        this.reward = reward;
        this.votesFor = 0;
        this.votesAgainst = 0;
        this.currentStreak = 0;
        this.consecutiveMisses = 0;
        this.lastLoggedTimestamp = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getIdentityId() { return identityId; }
    public void setIdentityId(long identityId) { this.identityId = identityId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isGoodHabit() { return isGoodHabit; }
    public void setGoodHabit(boolean goodHabit) { isGoodHabit = goodHabit; }

    public String getCue() { return cue; }
    public void setCue(String cue) { this.cue = cue; }

    public String getCraving() { return craving; }
    public void setCraving(String craving) { this.craving = craving; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }

    public int getVotesFor() { return votesFor; }
    public void setVotesFor(int votesFor) { this.votesFor = votesFor; }

    public int getVotesAgainst() { return votesAgainst; }
    public void setVotesAgainst(int votesAgainst) { this.votesAgainst = votesAgainst; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getConsecutiveMisses() { return consecutiveMisses; }
    public void setConsecutiveMisses(int consecutiveMisses) { this.consecutiveMisses = consecutiveMisses; }

    public long getLastLoggedTimestamp() { return lastLoggedTimestamp; }
    public void setLastLoggedTimestamp(long lastLoggedTimestamp) { this.lastLoggedTimestamp = lastLoggedTimestamp; }
}
