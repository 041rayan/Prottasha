package com.prottasha.quality;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public final class RemainingScreensVisualStructureTest {

    private final Context context = new ContextThemeWrapper(
            ApplicationProvider.getApplicationContext(),
            R.style.Theme_Prottasha);

    @Test
    public void settingsSecureLayoutExposesGroupedRowsAndDestructiveSeparation() {
        View root = inflate(R.layout.activity_settings_secure);

        View profile = root.findViewById(R.id.settings_profile);
        View account = root.findViewById(R.id.settings_account);
        View privacy = root.findViewById(R.id.settings_privacy);
        View guard = root.findViewById(R.id.settings_guard_permission);
        View focus = root.findViewById(R.id.settings_focus);
        View methodology = root.findViewById(R.id.settings_methodology);

        assertNotNull(profile);
        assertNotNull(account);
        assertNotNull(privacy);
        assertNotNull(guard);
        assertNotNull(focus);
        assertNotNull(methodology);

        assertMinimumTouchTarget(profile);
        assertMinimumTouchTarget(account);
        assertMinimumTouchTarget(privacy);
        assertMinimumTouchTarget(guard);
        assertMinimumTouchTarget(focus);
        assertMinimumTouchTarget(methodology);
    }

    @Test
    public void accountLayoutExposesSecurityAndDestructiveSeparation() {
        View root = inflate(R.layout.activity_account);

        assertNotNull(root.findViewById(R.id.account_email_display));
        assertNotNull(root.findViewById(R.id.account_status));
        assertNotNull(root.findViewById(R.id.account_confirm_checkbox));

        assertMinimumTouchTarget(root.findViewById(R.id.account_edit_profile_button));
        assertMinimumTouchTarget(root.findViewById(R.id.account_change_password_button));
        assertMinimumTouchTarget(root.findViewById(R.id.account_change_email_button));
        assertMinimumTouchTarget(root.findViewById(R.id.account_sign_out_button));
        assertMinimumTouchTarget(root.findViewById(R.id.account_switch_button));
        assertMinimumTouchTarget(root.findViewById(R.id.account_remove_local_button));
        assertMinimumTouchTarget(root.findViewById(R.id.account_delete_button));
    }

    @Test
    public void privacyAndAppLockLayoutsExposePinInputsAndActions() {
        View privacyRoot = inflate(R.layout.activity_privacy);
        assertNotNull(privacyRoot.findViewById(R.id.privacy_status));
        assertMinimumTouchTarget(privacyRoot.findViewById(R.id.privacy_lock_setup_button));
        assertMinimumTouchTarget(privacyRoot.findViewById(R.id.privacy_export));
        assertMinimumTouchTarget(privacyRoot.findViewById(R.id.privacy_import));

        View lockRoot = inflate(R.layout.activity_app_lock);
        assertNotNull(lockRoot.findViewById(R.id.app_lock_pin_display));
        assertMinimumTouchTarget(lockRoot.findViewById(R.id.btn_num_1));
        assertMinimumTouchTarget(lockRoot.findViewById(R.id.btn_num_0));
        assertMinimumTouchTarget(lockRoot.findViewById(R.id.btn_biometric));
        assertMinimumTouchTarget(lockRoot.findViewById(R.id.btn_backspace));
        assertMinimumTouchTarget(lockRoot.findViewById(R.id.app_lock_forgot_pin));

        View setupRoot = inflate(R.layout.activity_app_lock_setup);
        assertNotNull(setupRoot.findViewById(R.id.app_lock_new_pin_input));
        assertNotNull(setupRoot.findViewById(R.id.app_lock_confirm_pin_input));
        assertMinimumTouchTarget(setupRoot.findViewById(R.id.app_lock_save_pin_button));
    }

    @Test
    public void focusAndInterventionLayoutsExposeStepBackProminence() {
        View focusRoot = inflate(R.layout.activity_focus);
        assertNotNull(focusRoot.findViewById(R.id.focus_countdown));
        assertNotNull(focusRoot.findViewById(R.id.focus_status_label));
        assertNotNull(focusRoot.findViewById(R.id.focus_minutes));
        assertMinimumTouchTarget(focusRoot.findViewById(R.id.focus_start));
        assertMinimumTouchTarget(focusRoot.findViewById(R.id.focus_pause));
        assertMinimumTouchTarget(focusRoot.findViewById(R.id.focus_resume));
        assertMinimumTouchTarget(focusRoot.findViewById(R.id.focus_end));

        View overlayRoot = inflate(R.layout.view_intervention_overlay);
        assertNotNull(overlayRoot.findViewById(R.id.intervention_title));
        assertNotNull(overlayRoot.findViewById(R.id.intervention_target));
        assertNotNull(overlayRoot.findViewById(R.id.intervention_intention));
        assertMinimumTouchTarget(overlayRoot.findViewById(R.id.intervention_step_back));
        assertMinimumTouchTarget(overlayRoot.findViewById(R.id.intervention_focus_button));
        assertMinimumTouchTarget(overlayRoot.findViewById(R.id.intervention_continue));

        View pauseRoot = inflate(R.layout.activity_pause_interstitial);
        assertNotNull(pauseRoot.findViewById(R.id.tvPauseQuestion));
        assertNotNull(pauseRoot.findViewById(R.id.tvPauseCountdown));
        assertMinimumTouchTarget(pauseRoot.findViewById(R.id.btnStepBack));
        assertMinimumTouchTarget(pauseRoot.findViewById(R.id.btnProceedWithIntention));
    }

    @Test
    public void supportRehearsalAndReflectionLayoutsExposePrimaryActions() {
        View supportRoot = inflate(R.layout.activity_support);
        assertNotNull(supportRoot.findViewById(R.id.support_entries));
        assertMinimumTouchTarget(supportRoot.findViewById(R.id.support_refresh_button));

        View rehearsalRoot = inflate(R.layout.activity_rehearsal);
        assertNotNull(rehearsalRoot.findViewById(R.id.rehearsal_scenario));
        assertNotNull(rehearsalRoot.findViewById(R.id.rehearsal_intensity_slider));
        assertMinimumTouchTarget(rehearsalRoot.findViewById(R.id.rehearsal_complete_button));

        View reflectionRoot = inflate(R.layout.activity_reflection);
        assertNotNull(reflectionRoot.findViewById(R.id.reflection_fact));
        assertNotNull(reflectionRoot.findViewById(R.id.reflection_interpretation));
        assertNotNull(reflectionRoot.findViewById(R.id.reflection_lesson));
        assertMinimumTouchTarget(reflectionRoot.findViewById(R.id.reflection_save));
        assertMinimumTouchTarget(reflectionRoot.findViewById(R.id.reflection_attach_image));
    }

    private View inflate(int layoutResId) {
        AtomicReference<View> ref = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            ref.set(LayoutInflater.from(context).inflate(layoutResId, null, false));
        });
        return ref.get();
    }

    private void assertMinimumTouchTarget(View view) {
        assertNotNull("Target view must not be null", view);
        int specWidth = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.AT_MOST);
        int specHeight = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST);
        view.measure(specWidth, specHeight);

        float density = context.getResources().getDisplayMetrics().density;
        int minPx = Math.round(48f * density);

        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();

        boolean meetsWidth = measuredWidth >= minPx || view.getLayoutParams().width == -1;
        boolean meetsHeight = measuredHeight >= minPx;

        assertTrue(
                "Touch target too small: " + view.getClass().getSimpleName()
                        + " measured " + measuredWidth + "x" + measuredHeight + "px"
                        + ", expected at least " + minPx + "px (48dp)",
                meetsWidth && meetsHeight
        );
    }
}
