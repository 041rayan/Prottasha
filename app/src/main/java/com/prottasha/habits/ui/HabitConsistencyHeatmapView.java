package com.prottasha.habits.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.prottasha.R;

public final class HabitConsistencyHeatmapView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellBounds = new RectF();
    private final float density;
    @Nullable private HabitConsistencyGridState state;

    public HabitConsistencyHeatmapView(Context context) {
        this(context, null);
    }

    public HabitConsistencyHeatmapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HabitConsistencyHeatmapView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = getResources().getDisplayMetrics().density;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    public void setState(@Nullable HabitConsistencyGridState state) {
        this.state = state;
        setContentDescription(state == null ? null : state.accessibilitySummary);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int fallbackWidth = Math.round(320 * density) + getPaddingLeft() + getPaddingRight();
        int width = resolveSize(fallbackWidth, widthMeasureSpec);
        int weeks = state == null ? 12 : state.weeks;
        float gap = 4 * density;
        float availableWidth = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        float cell = Math.max(1, (availableWidth - gap * (weeks - 1)) / weeks);
        int desiredHeight = getPaddingTop() + getPaddingBottom()
                + Math.round(cell * 7 + gap * 6);
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (state == null || state.cells.isEmpty()) return;

        float gap = 4 * density;
        float availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float cell = Math.max(1, (availableWidth - gap * (state.weeks - 1)) / state.weeks);
        float radius = Math.min(4 * density, cell / 4f);

        for (int index = 0; index < state.cells.size(); index++) {
            HabitConsistencyGridState.Cell value = state.cells.get(index);
            int column = index / 7;
            int row = index % 7;
            float left = getPaddingLeft() + column * (cell + gap);
            float top = getPaddingTop() + row * (cell + gap);
            cellBounds.set(left, top, left + cell, top + cell);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colorFor(value.intensity));
            canvas.drawRoundRect(cellBounds, radius, radius, paint);

            if (value.currentDay) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2 * density);
                paint.setColor(ContextCompat.getColor(getContext(), R.color.color_identity));
                float inset = density;
                cellBounds.inset(inset, inset);
                canvas.drawRoundRect(cellBounds, radius, radius, paint);
            }
        }
    }

    private int colorFor(int intensity) {
        int color;
        switch (intensity) {
            case 1:
                color = R.color.color_consistency_level_1;
                break;
            case 2:
                color = R.color.color_consistency_level_2;
                break;
            case 3:
                color = R.color.color_consistency_level_3;
                break;
            case 4:
                color = R.color.color_consistency_level_4;
                break;
            default:
                color = R.color.color_surface_raised;
                break;
        }
        return ContextCompat.getColor(getContext(), color);
    }
}
