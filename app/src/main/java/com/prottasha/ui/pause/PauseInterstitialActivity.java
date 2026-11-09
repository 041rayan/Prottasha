package com.prottasha.ui.pause;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.intervention.ui.InterventionOverlayView;

public final class PauseInterstitialActivity extends AppCompatActivity {
    public static final String EXTRA_TARGET_PACKAGE = "EXTRA_TARGET_PACKAGE";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String target = getIntent().getStringExtra(EXTRA_TARGET_PACKAGE);
        boolean block = "BLOCK".equals(getIntent().getStringExtra("guard_mode"));
        setContentView(new InterventionOverlayView(
                this, target == null ? "Selected app" : target, block,
                new InterventionOverlayView.Listener() {
                    @Override public void onStepBack() {
                        Intent home = new Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_HOME)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(home);
                        finish();
                    }

                    @Override public void onContinue(String intention) {
                        finish();
                    }
                }));
    }
}
