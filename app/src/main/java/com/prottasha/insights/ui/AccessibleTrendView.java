package com.prottasha.insights.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.prottasha.R;

public final class AccessibleTrendView extends View {
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private final RectF progressBounds = new RectF();
    private double rate = 0.0;
    private int decisions = 0;

    public AccessibleTrendView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMinimumHeight(48);
        trackPaint.setColor(getResources().getColor(R.color.color_surface_raised, null));
        progressPaint.setColor(getResources().getColor(R.color.color_hope, null));
    }

    public void setRate(double rate, int decisions) {
        this.rate = Math.max(0.0, Math.min(1.0, rate));
        this.decisions = decisions;
        if (decisions == 0) {
            setContentDescription(getContext().getString(R.string.insights_no_data));
        } else {
            setContentDescription(getContext().getString(
                    R.string.insights_chart_description, Math.round(this.rate * 100), decisions));
        }
        invalidate();
    }

    public void setData(double followThroughRate, int totalDecisions, int stepBacks, int rapidReopens, int continues) {
        this.rate = Math.max(0.0, Math.min(1.0, followThroughRate));
        this.decisions = totalDecisions;
        if (totalDecisions == 0) {
            setContentDescription(getContext().getString(R.string.insights_no_data));
        } else {
            String desc = getContext().getString(
                    R.string.insights_chart_description, Math.round(this.rate * 100), totalDecisions)
                    + String.format(", %d step backs, %d rapid reopens, %d continued.",
                    stepBacks, rapidReopens, continues);
            setContentDescription(desc);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float radius = h / 4f;
        bounds.set(0, 0, w, h);
        canvas.drawRoundRect(bounds, radius, radius, trackPaint);

        if (rate > 0.0) {
            float progressWidth = Math.max(radius * 2, (float) (w * rate));
            progressBounds.set(0, 0, progressWidth, h);
            canvas.drawRoundRect(progressBounds, radius, radius, progressPaint);
        }
    }
}
