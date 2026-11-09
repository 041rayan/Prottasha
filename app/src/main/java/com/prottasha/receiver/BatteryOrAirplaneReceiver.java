package com.prottasha.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BatteryOrAirplaneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_BATTERY_LOW.equals(action)) {
            Toast.makeText(context, "Low Battery: Prottasha conserving sync and background telemetry.", Toast.LENGTH_LONG).show();
        } else if (Intent.ACTION_BATTERY_OKAY.equals(action)) {
            Toast.makeText(context, "Battery Recovered: Resuming regular focus telemetry.", Toast.LENGTH_SHORT).show();
        } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
            if (isAirplaneModeOn) {
                Toast.makeText(context, "Airplane Mode Active: Switched to 100% offline Room database.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Network Restored: Syncing pod encouragement queue.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
