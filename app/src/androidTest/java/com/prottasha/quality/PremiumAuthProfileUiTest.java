package com.prottasha.quality;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.button.MaterialButton;
import com.prottasha.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class PremiumAuthProfileUiTest {

    private final Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void signInRetainsAccessibleFormAndPremiumBranding() {
        View root = inflate(R.layout.fragment_sign_in);
        assertNotNull(root.findViewById(R.id.auth_brand_logo));
        assertNotNull(root.findViewById(R.id.auth_email_input));
        assertNotNull(root.findViewById(R.id.auth_password_input));
        assertNotNull(root.findViewById(R.id.auth_privacy_note));
        assertMinimumTouchTarget(root.findViewById(R.id.auth_sign_in_button));
        assertMinimumTouchTarget(root.findViewById(R.id.auth_create_link));
    }

    @Test
    public void profileRetainsPhotoAndIdentityControls() {
        View root = inflate(R.layout.activity_profile);
        assertNotNull(root.findViewById(R.id.profile_photo_image));
        assertNotNull(root.findViewById(R.id.profile_photo_edit_button));
        assertNotNull(root.findViewById(R.id.profile_choose_photo_button));
        assertNotNull(root.findViewById(R.id.profile_remove_photo_button));
        assertNotNull(root.findViewById(R.id.profile_private_section_label));
        assertNotNull(root.findViewById(R.id.profile_circle_section_label));
        assertMinimumTouchTarget(root.findViewById(R.id.profile_save_button));
    }

    private View inflate(int layout) {
        return LayoutInflater.from(context).inflate(layout, null, false);
    }

    private void assertMinimumTouchTarget(View view) {
        int spec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.AT_MOST);
        view.measure(spec, View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST));
        float density = context.getResources().getDisplayMetrics().density;
        assertTrue(view.getMeasuredHeight() >= 48 * density);
    }
}
