package com.prottasha.guard.domain;

public final class GuardModels {
    public static final class GuardContext {
        public final int minuteOfDay;
        public final boolean serviceHealthy;
        public final boolean essentialTarget;

        public GuardContext(int minuteOfDay, boolean serviceHealthy, boolean essentialTarget) {
            this.minuteOfDay = minuteOfDay;
            this.serviceHealthy = serviceHealthy;
            this.essentialTarget = essentialTarget;
        }
    }

    public static final class GuardUsage {
        public final int usedMinutes;
        public final boolean qualityAvailable;
        public final boolean cooldownActive;

        public GuardUsage(int usedMinutes, boolean qualityAvailable, boolean cooldownActive) {
            this.usedMinutes = usedMinutes;
            this.qualityAvailable = qualityAvailable;
            this.cooldownActive = cooldownActive;
        }
    }

    private GuardModels() { }
}
