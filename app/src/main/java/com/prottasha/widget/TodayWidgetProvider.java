package com.prottasha.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.prottasha.R;
import com.prottasha.auth.ui.LaunchActivity;

public final class TodayWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today);
            views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_neutral_title));
            views.setTextViewText(R.id.widget_body, context.getString(R.string.widget_neutral_body));
            PendingIntent open = PendingIntent.getActivity(context, 0,
                    new Intent(context, LaunchActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, open);
            manager.updateAppWidget(widgetId, views);
        }
    }
}
