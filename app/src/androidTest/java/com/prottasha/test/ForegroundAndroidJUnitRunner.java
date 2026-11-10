package com.prottasha.test;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.runner.AndroidJUnitRunner;

import com.prottasha.auth.ui.AuthActivity;

public final class ForegroundAndroidJUnitRunner extends AndroidJUnitRunner {

    private Bundle arguments;

    @Override
    public void onCreate(Bundle arguments) {
        this.arguments = new Bundle(arguments);
        super.onCreate(arguments);
    }

    @Override
    public void onStart() {
        String requestedClass = arguments.getString("class", "");
        String requestedPackage = arguments.getString("package", "");
        if (!requestedClass.isEmpty() || !requestedPackage.isEmpty()) {
            Intent host = new Intent(getTargetContext(), AuthActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivitySync(host);
        }
        super.onStart();
    }
}
