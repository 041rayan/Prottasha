package com.prottasha.quality;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prottasha.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public final class HabitsGuardVisualStructureTest {

    private final Context context = new ContextThemeWrapper(
            ApplicationProvider.getApplicationContext(),
            R.style.Theme_Prottasha);

    @Test
    public void habitsFragmentExposesRecyclerViewFiltersAndAddAction() {
        View root = inflate(R.layout.fragment_habits);

        View recyclerView = root.findViewById(R.id.habits_container);
        assertNotNull(recyclerView);
        assertTrue(recyclerView instanceof RecyclerView);

        View addButton = root.findViewById(R.id.habits_add_button);
        assertNotNull(addButton);
        assertMinimumTouchTarget(addButton);

        assertNotNull(root.findViewById(R.id.habits_filter_group));
        assertNotNull(root.findViewById(R.id.habits_filter_all));
        assertNotNull(root.findViewById(R.id.habits_filter_build));
        assertNotNull(root.findViewById(R.id.habits_filter_avoid));
        assertNotNull(root.findViewById(R.id.habits_filter_amount));
        assertNotNull(root.findViewById(R.id.habits_filter_archived));
        assertNotNull(root.findViewById(R.id.habits_empty));
    }

    @Test
    public void habitRowKeepsActionControlsAndTouchTarget() {
        View row = inflate(R.layout.item_habit_today);

        assertNotNull(row.findViewById(R.id.habit_title));
        assertNotNull(row.findViewById(R.id.habit_type));
        assertNotNull(row.findViewById(R.id.habit_progress));
        assertNotNull(row.findViewById(R.id.habit_move_up));
        assertNotNull(row.findViewById(R.id.habit_move_down));
        assertNotNull(row.findViewById(R.id.habit_archive));

        View actionBtn = row.findViewById(R.id.habit_action);
        assertNotNull(actionBtn);
        assertMinimumTouchTarget(actionBtn);
    }

    @Test
    public void guardFragmentExposesRulesRecyclerTabsAndActions() {
        View root = inflate(R.layout.fragment_guard);

        View rulesList = root.findViewById(R.id.guard_rules);
        assertNotNull(rulesList);
        assertTrue(rulesList instanceof RecyclerView);

        View addApp = root.findViewById(R.id.guard_add_button);
        assertNotNull(addApp);
        assertMinimumTouchTarget(addApp);

        View addWeb = root.findViewById(R.id.guard_add_website_button);
        assertNotNull(addWeb);
        assertMinimumTouchTarget(addWeb);

        assertNotNull(root.findViewById(R.id.guard_health_card));
        assertNotNull(root.findViewById(R.id.guard_search_input));
        assertNotNull(root.findViewById(R.id.guard_filter_group));
        assertNotNull(root.findViewById(R.id.guard_empty));
        assertNotNull(root.findViewById(R.id.guard_no_matches));
    }

    @Test
    public void guardRuleItemRetainsToggleAndMetadata() {
        View row = inflate(R.layout.item_guard_rule);

        assertNotNull(row.findViewById(R.id.guard_rule_item_label));
        assertNotNull(row.findViewById(R.id.guard_rule_item_target));
        assertNotNull(row.findViewById(R.id.guard_rule_item_mode_badge));

        View toggle = row.findViewById(R.id.guard_rule_item_toggle);
        assertNotNull(toggle);
        assertMinimumTouchTarget(toggle);
    }

    @Test
    public void editorsKeepStickySaveAndFormControls() {
        View habitEdit = inflate(R.layout.activity_habit_edit);
        assertNotNull(habitEdit.findViewById(R.id.habit_title_input));
        assertMinimumTouchTarget(habitEdit.findViewById(R.id.habit_save_button));

        View guardEdit = inflate(R.layout.activity_guard_rule_edit);
        assertNotNull(guardEdit.findViewById(R.id.guard_mode_pause));
        assertNotNull(guardEdit.findViewById(R.id.guard_mode_budget));
        assertNotNull(guardEdit.findViewById(R.id.guard_mode_block));
        assertMinimumTouchTarget(guardEdit.findViewById(R.id.guard_rule_save));
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
