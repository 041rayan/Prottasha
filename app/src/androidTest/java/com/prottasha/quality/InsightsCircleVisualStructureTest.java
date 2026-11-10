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
public final class InsightsCircleVisualStructureTest {

    private final Context context = new ContextThemeWrapper(
            ApplicationProvider.getApplicationContext(),
            R.style.Theme_Prottasha);

    @Test
    public void insightsFragmentExposesPrimaryMetricTrendAndActions() {
        View root = inflate(R.layout.fragment_insights);

        assertNotNull(root.findViewById(R.id.insights_summary));
        assertNotNull(root.findViewById(R.id.insights_trend));
        assertNotNull(root.findViewById(R.id.insights_range_group));
        assertNotNull(root.findViewById(R.id.chip_today));
        assertNotNull(root.findViewById(R.id.chip_week));
        assertNotNull(root.findViewById(R.id.chip_month));
        assertNotNull(root.findViewById(R.id.insights_targets_recycler));

        assertMinimumTouchTarget(root.findViewById(R.id.insights_reflect));
        assertMinimumTouchTarget(root.findViewById(R.id.insights_add_trigger));
        assertMinimumTouchTarget(root.findViewById(R.id.insights_add_memory));
        assertMinimumTouchTarget(root.findViewById(R.id.insights_rehearsal));
        assertMinimumTouchTarget(root.findViewById(R.id.insights_methodology));
    }

    @Test
    public void circleFragmentExposesClosedPodAccountabilityAndSupport() {
        View root = inflate(R.layout.fragment_circle);

        assertNotNull(root.findViewById(R.id.circle_status));

        View openPod = root.findViewById(R.id.circle_open_pod);
        assertNotNull(openPod);
        assertMinimumTouchTarget(openPod);

        View accountability = root.findViewById(R.id.circle_accountability);
        assertNotNull(accountability);
        assertMinimumTouchTarget(accountability);

        View crisisHelp = root.findViewById(R.id.circle_crisis_help);
        assertNotNull(crisisHelp);
        assertMinimumTouchTarget(crisisHelp);
    }

    @Test
    public void podChatLayoutExposesHeaderComposerAndSendAction() {
        View root = inflate(R.layout.activity_pod_chat_new);

        assertNotNull(root.findViewById(R.id.pod_header_title));
        assertNotNull(root.findViewById(R.id.pod_message_input));

        View sendButton = root.findViewById(R.id.pod_send);
        assertNotNull(sendButton);
        assertMinimumTouchTarget(sendButton);

        View backButton = root.findViewById(R.id.pod_back_button);
        assertNotNull(backButton);
        assertMinimumTouchTarget(backButton);
    }

    @Test
    public void accountabilityLayoutExposesPartnerAndActions() {
        View root = inflate(R.layout.activity_accountability);

        assertNotNull(root.findViewById(R.id.accountability_name_input));
        assertNotNull(root.findViewById(R.id.accountability_phone_input));

        assertMinimumTouchTarget(root.findViewById(R.id.accountability_save_button));
        assertMinimumTouchTarget(root.findViewById(R.id.accountability_pick));
        assertMinimumTouchTarget(root.findViewById(R.id.accountability_share));
    }

    private View inflate(int layoutResId) {
        AtomicReference<View> ref = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            ref.set(LayoutInflater.from(context).inflate(layoutResId, null, false));
        });
        return ref.get();
    }

    private void assertMinimumTouchTarget(View view) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST);
        view.measure(widthSpec, heightSpec);
        float density = context.getResources().getDisplayMetrics().density;
        assertTrue(
                "View " + view.getId() + " measured height " + view.getMeasuredHeight() + " is under 48dp",
                view.getMeasuredHeight() >= 48 * density);
    }
}
