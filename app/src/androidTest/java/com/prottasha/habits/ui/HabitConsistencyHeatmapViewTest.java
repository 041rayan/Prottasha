package com.prottasha.habits.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public final class HabitConsistencyHeatmapViewTest {

    @Test
    public void heatmapExposesStateSummaryAndMeasuresAllSevenRows() {
        Context context = ApplicationProvider.getApplicationContext();
        HabitConsistencyGridState state = HabitConsistencyGridBuilder.build(
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDate.of(2026, 11, 10),
                12);
        HabitConsistencyHeatmapView view = new HabitConsistencyHeatmapView(context);

        view.setState(state);
        view.measure(
                View.MeasureSpec.makeMeasureSpec(640, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        assertEquals(state.accessibilitySummary, view.getContentDescription());
        assertEquals(640, view.getMeasuredWidth());
        assertTrue(view.getMeasuredHeight() > 0);
    }
}
