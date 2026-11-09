package com.prottasha.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "journal_entries")
public class JournalEntryEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long dateEpochDay;
    private int identityAlignmentScore;
    private int positiveVotesToday;
    private int negativeVotesToday;
    private String winsText;
    private String chainReviewText;
    private boolean isWeeklyReview;

    public JournalEntryEntity(long dateEpochDay, int identityAlignmentScore, int positiveVotesToday, int negativeVotesToday, String winsText, String chainReviewText, boolean isWeeklyReview) {
        this.dateEpochDay = dateEpochDay;
        this.identityAlignmentScore = identityAlignmentScore;
        this.positiveVotesToday = positiveVotesToday;
        this.negativeVotesToday = negativeVotesToday;
        this.winsText = winsText;
        this.chainReviewText = chainReviewText;
        this.isWeeklyReview = isWeeklyReview;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getDateEpochDay() { return dateEpochDay; }
    public void setDateEpochDay(long dateEpochDay) { this.dateEpochDay = dateEpochDay; }

    public int getIdentityAlignmentScore() { return identityAlignmentScore; }
    public void setIdentityAlignmentScore(int identityAlignmentScore) { this.identityAlignmentScore = identityAlignmentScore; }

    public int getPositiveVotesToday() { return positiveVotesToday; }
    public void setPositiveVotesToday(int positiveVotesToday) { this.positiveVotesToday = positiveVotesToday; }

    public int getNegativeVotesToday() { return negativeVotesToday; }
    public void setNegativeVotesToday(int negativeVotesToday) { this.negativeVotesToday = negativeVotesToday; }

    public String getWinsText() { return winsText; }
    public void setWinsText(String winsText) { this.winsText = winsText; }

    public String getChainReviewText() { return chainReviewText; }
    public void setChainReviewText(String chainReviewText) { this.chainReviewText = chainReviewText; }

    public boolean isWeeklyReview() { return isWeeklyReview; }
    public void setWeeklyReview(boolean weeklyReview) { isWeeklyReview = weeklyReview; }
}
