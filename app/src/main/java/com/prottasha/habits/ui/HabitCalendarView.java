package com.prottasha.habits.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.prottasha.R;
import com.prottasha.habits.data.HabitDayAggregateEntity;

import java.util.Collections;
import java.util.List;

public final class HabitCalendarView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<HabitDayAggregateEntity> values = Collections.emptyList();

    public HabitCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMinimumHeight((int) (96 * getResources().getDisplayMetrics().density));
    }

    public void setValues(List<HabitDayAggregateEntity> values) {
        this.values = values == null ? Collections.emptyList() : values;
        int completed = 0;
        for (HabitDayAggregateEntity value : this.values) {
            if (value.completed) completed++;
        }
        setContentDescription(getResources().getQuantityString(
                R.plurals.habit_history_days, this.values.size(), completed, this.values.size()));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float gap = 6 * getResources().getDisplayMetrics().density;
        float size = Math.max(12, (getWidth() - gap * 6) / 7f);
        paint.setColor(getResources().getColor(R.color.color_surface_raised, null));
        for (int index = 0; index < 28; index++) {
            int row = index / 7;
            int column = index % 7;
            if (index < values.size()) {
                HabitDayAggregateEntity value = values.get(index);
                paint.setColor(getResources().getColor(
                        value.lapsed ? R.color.color_crisis
                                : value.completed ? R.color.color_hope
                                : R.color.color_surface_raised, null));
            } else {
                paint.setColor(getResources().getColor(R.color.color_surface_raised, null));
            }
            float left = column * (size + gap);
            float top = row * (size + gap);
            canvas.drawRoundRect(left, top, left + size, top + size, 6, 6, paint);
        }
    }
}
