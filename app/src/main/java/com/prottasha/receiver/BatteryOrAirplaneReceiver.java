package com.prottasha.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prottasha.ProttashaApp;
import com.prottasha.core.platform.NetworkPowerStateRepository;

public final class BatteryOrAirplaneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        ProttashaApp app = (ProttashaApp) context.getApplicationContext();
        NetworkPowerStateRepository.State current = app.appGraph().networkPowerState().current();
        boolean airplane = current.airplaneMode;
        boolean lowBattery = current.lowBattery;
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
            airplane = intent.getBooleanExtra("state", false);
        } else if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            lowBattery = true;
        } else if (Intent.ACTION_BATTERY_OKAY.equals(intent.getAction())) {
            lowBattery = false;
        }
        app.appGraph().networkPowerState().update(
                airplane, lowBattery, current.networkAvailable);
    }
}
