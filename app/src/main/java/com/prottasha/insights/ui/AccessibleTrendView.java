package com.prottasha.insights.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.prottasha.R;

public final class AccessibleTrendView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double rate;

    public AccessibleTrendView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMinimumHeight(80);
    }

    public void setRate(double rate, int decisions) {
        this.rate = Math.max(0, Math.min(1, rate));
        setContentDescription(getContext().getString(
                R.string.insights_chart_description, Math.round(this.rate * 100), decisions));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        paint.setColor(getResources().getColor(R.color.color_surface_raised, null));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setColor(getResources().getColor(R.color.color_hope, null));
        canvas.drawRect(0, 0, (float) (getWidth() * rate), getHeight(), paint);
    }
}
