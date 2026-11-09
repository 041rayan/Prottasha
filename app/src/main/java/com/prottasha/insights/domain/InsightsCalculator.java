package com.prottasha.insights.domain;

import java.util.List;

public final class InsightsCalculator {
    public enum Outcome { STEP_BACK, RAPID_REOPEN, FOLLOW_THROUGH, CONTINUE, BLOCKED }

    public static final class Snapshot {
        public final boolean hasData;
        public final double followThroughRate;
        public final boolean adequateSample;
        public final int decisions;

        Snapshot(boolean hasData, double followThroughRate,
                boolean adequateSample, int decisions) {
            this.hasData = hasData;
            this.followThroughRate = followThroughRate;
            this.adequateSample = adequateSample;
            this.decisions = decisions;
        }
    }

    private InsightsCalculator() { }

    public static Snapshot calculate(List<Outcome> outcomes) {
        if (outcomes.isEmpty()) return new Snapshot(false, 0.0, false, 0);
        int followedThrough = 0;
        for (Outcome outcome : outcomes) {
            if (outcome == Outcome.FOLLOW_THROUGH) followedThrough++;
        }
        return new Snapshot(true, followedThrough / (double) outcomes.size(),
                outcomes.size() >= 7, outcomes.size());
    }
}
