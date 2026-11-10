package com.prottasha.today;

public final class HopeHorizonState {
    public final int totalPrompts;
    public final int mindfulExits;
    public final int intentionalContinues;

    public HopeHorizonState(int totalPrompts, int mindfulExits, int intentionalContinues) {
        this.totalPrompts = totalPrompts;
        this.mindfulExits = mindfulExits;
        this.intentionalContinues = intentionalContinues;
    }

    public boolean hasData() {
        return totalPrompts > 0;
    }

    public int mindfulExitPercentage() {
        return totalPrompts > 0 ? (int) Math.round((mindfulExits * 100.0) / totalPrompts) : 0;
    }
}
