package com.prottasha.core.platform;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class NetworkPowerStateRepositoryTest {
    @Test
    public void airplaneAndLowBatteryPauseCloudWithoutDisablingLocalWrites() {
        NetworkPowerStateRepository.State state = NetworkPowerStateRepository.reduce(
                NetworkPowerStateRepository.State.normal(), true, false, false);
        assertTrue(state.airplaneMode);
        assertTrue(state.pauseCloudRefresh());
        assertTrue(state.localWritesAllowed());

        state = NetworkPowerStateRepository.reduce(state, false, true, true);
        assertFalse(state.airplaneMode);
        assertTrue(state.lowBattery);
        assertTrue(state.networkAvailable);
        assertTrue(state.pauseCloudRefresh());
    }
}
