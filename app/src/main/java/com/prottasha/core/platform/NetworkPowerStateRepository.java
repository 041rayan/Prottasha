package com.prottasha.core.platform;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public final class NetworkPowerStateRepository {
    public static final class State {
        public final boolean airplaneMode;
        public final boolean lowBattery;
        public final boolean networkAvailable;

        public State(boolean airplaneMode, boolean lowBattery, boolean networkAvailable) {
            this.airplaneMode = airplaneMode;
            this.lowBattery = lowBattery;
            this.networkAvailable = networkAvailable;
        }

        public static State normal() { return new State(false, false, true); }
        public boolean pauseCloudRefresh() { return airplaneMode || lowBattery || !networkAvailable; }
        public boolean localWritesAllowed() { return true; }
    }

    private final MutableLiveData<State> state = new MutableLiveData<>(State.normal());

    public LiveData<State> observe() { return state; }
    public State current() { return state.getValue(); }
    public void update(boolean airplane, boolean lowBattery, boolean network) {
        state.postValue(reduce(current(), airplane, lowBattery, network));
    }

    public static State reduce(State previous, boolean airplane, boolean lowBattery,
            boolean networkAvailable) {
        return new State(airplane, lowBattery, networkAvailable);
    }
}
