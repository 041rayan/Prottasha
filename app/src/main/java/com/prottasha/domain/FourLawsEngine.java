package com.prottasha.domain;

public class FourLawsEngine {

    public enum HabitLaw {
        OBVIOUS(1, "Make it Obvious", "Make it Invisible"),
        ATTRACTIVE(2, "Make it Attractive", "Make it Unattractive"),
        EASY(3, "Make it Easy", "Make it Difficult"),
        SATISFYING(4, "Make it Satisfying", "Make it Unsatisfying");

        private final int lawNumber;
        private final String buildTitle;
        private final String breakTitle;

        HabitLaw(int lawNumber, String buildTitle, String breakTitle) {
            this.lawNumber = lawNumber;
            this.buildTitle = buildTitle;
            this.breakTitle = breakTitle;
        }

        public int getLawNumber() { return lawNumber; }
        public String getBuildTitle() { return buildTitle; }
        public String getBreakTitle() { return breakTitle; }
    }

    /**
     * Scales any action down to its Two-Minute starter step (AH-14).
     */
    public static String scaleToTwoMinuteVersion(String fullHabit) {
        if (fullHabit == null || fullHabit.trim().isEmpty()) {
            return "Take 3 deep breaths and prepare your environment";
        }
        String lower = fullHabit.toLowerCase();
        if (lower.contains("study") || lower.contains("read")) {
            return "Open notes and read just one page or paragraph (2 min)";
        } else if (lower.contains("workout") || lower.contains("exercise") || lower.contains("run")) {
            return "Put on running shoes and do 5 light stretches (2 min)";
        } else if (lower.contains("meditat") || lower.contains("visualiz")) {
            return "Sit comfortably and relax shoulders for 2 minutes";
        } else if (lower.contains("write") || lower.contains("code")) {
            return "Open IDE/document and write 2 lines of code/notes";
        }
        return "Start the first step of: " + fullHabit + " for 2 minutes";
    }

    /**
     * Determines whether an identity election is in majority (AH-01).
     */
    public static boolean isIdentityMajority(int votesFor, int votesAgainst) {
        return votesFor > votesAgainst;
    }

    /**
     * Calculates the Identity Alignment Percentage (0% to 100%).
     */
    public static int calculateAlignmentScore(int votesFor, int votesAgainst) {
        int total = votesFor + votesAgainst;
        if (total == 0) return 100;
        return (int) Math.round(((double) votesFor / total) * 100.0);
    }

    /**
     * Provides non-shaming, constructive reframing for a temptation moment (AH-12, AX-6).
     */
    public static String getCognitiveReframe(String motive) {
        if (motive == null || motive.isEmpty()) {
            return "Notice the urge. It is just an ancient survival signal, not a mandate.";
        }
        return "Your brain is seeking " + motive + ". There are better ways to fulfill this than compulsive loops.";
    }
}
