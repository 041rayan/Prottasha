package com.prottasha.quality;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.widget.NestedScrollView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prottasha.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class PremiumTodayUiTest {

    private final Context context = new ContextThemeWrapper(
            ApplicationProvider.getApplicationContext(),
            R.style.Theme_Prottasha);

    @Test
    public void todayPresentsGreetingIdentityConsistencyAndPrimaryAction() {
        View root = LayoutInflater.from(context).inflate(R.layout.fragment_today, null, false);

        assertTrue(root instanceof NestedScrollView);
        assertNotNull(root.findViewById(R.id.today_date));
        assertNotNull(root.findViewById(R.id.today_greeting));
        assertNotNull(root.findViewById(R.id.today_avatar));
        assertNotNull(root.findViewById(R.id.today_identity_card));
        assertNotNull(root.findViewById(R.id.today_consistency_heatmap));
        assertNotNull(root.findViewById(R.id.today_consistency_active_days));
        assertNotNull(root.findViewById(R.id.today_consistency_best_run));
        assertNotNull(root.findViewById(R.id.today_consistency_percent));
        assertNotNull(root.findViewById(R.id.today_habits_container));
        assertNotNull(root.findViewById(R.id.today_boundaries_count));
        assertMinimumTouchTarget(root.findViewById(R.id.today_focus_button));
    }

    @Test
    public void todayHabitRowKeepsLongTextAndActionReachable() {
        View row = LayoutInflater.from(context).inflate(R.layout.item_today_habit, null, false);

        assertNotNull(row.findViewById(R.id.today_habit_title));
        assertNotNull(row.findViewById(R.id.today_habit_meta));
        assertMinimumTouchTarget(row.findViewById(R.id.today_habit_action));
    }

    private void assertMinimumTouchTarget(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.AT_MOST);
        int height = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST);
        view.measure(width, height);
        float density = context.getResources().getDisplayMetrics().density;
        assertTrue(view.getMeasuredHeight() >= 48 * density);
    }
}
