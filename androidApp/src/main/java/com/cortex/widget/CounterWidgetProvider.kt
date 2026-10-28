package com.cortex.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.cortex.R
import com.cortex.app.MainActivity
import com.cortex.core.WidgetBackgroundMode
import com.cortex.core.elapsedBreakdown
import com.cortex.core.minuteChronometerBase
import com.cortex.core.nowMillis
import com.cortex.data.database.CounterEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CounterWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        WidgetRefreshReceiver.scheduleNext(context)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateAll(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun refreshAllAsync(context: Context) {
            val appContext = context.applicationContext
            CoroutineScope(Dispatchers.IO).launch {
                refreshNow(appContext)
            }
        }

        suspend fun refreshNow(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext)
            val ids = manager.getAppWidgetIds(
                ComponentName(appContext, CounterWidgetProvider::class.java),
            )
            if (ids.isNotEmpty()) {
                updateAll(appContext, manager, ids)
            }
        }

        private suspend fun updateAll(
            context: Context,
            manager: AppWidgetManager,
            appWidgetIds: IntArray,
        ) {
            val counter = getDatabase(context).counterDao().getCounter()
            val background = WidgetBackgroundStore.load(context)
            val mode = WidgetBackgroundSettings.mode(context)
            val now = nowMillis()
            val elapsedRealtime = SystemClock.elapsedRealtime()
            for (appWidgetId in appWidgetIds) {
                val options = manager.getAppWidgetOptions(appWidgetId)
                val widgetHeight = options
                    .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                    .takeIf { it > 0 }
                    ?: options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
                val fillHeightDp = if (widgetHeight > 0) widgetHeight / 2f + 12f else 80f
                manager.updateAppWidget(
                    appWidgetId,
                    buildViews(
                        context = context,
                        appWidgetId = appWidgetId,
                        counter = counter,
                        background = background,
                        backgroundMode = mode,
                        fillHeightDp = fillHeightDp,
                        nowMillis = now,
                        elapsedRealtimeMillis = elapsedRealtime,
                    ),
                )
            }
        }

        private fun buildViews(
            context: Context,
            appWidgetId: Int,
            counter: CounterEntity?,
            background: WidgetBackground?,
            backgroundMode: WidgetBackgroundMode,
            fillHeightDp: Float,
            nowMillis: Long,
            elapsedRealtimeMillis: Long,
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_counter)

            val openIntent = Intent(context, MainActivity::class.java)
            val openPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, openPendingIntent)

            if (background != null) {
                val fit = backgroundMode == WidgetBackgroundMode.FIT_CENTER
                if (fit) {
                    views.setImageViewBitmap(
                        R.id.widget_background_image_fit,
                        background.bitmap,
                    )
                    views.setInt(
                        R.id.widget_top_fill,
                        "setBackgroundColor",
                        background.topColor,
                    )
                    views.setInt(
                        R.id.widget_bottom_fill,
                        "setBackgroundColor",
                        background.bottomColor,
                    )
                    views.setViewLayoutHeight(
                        R.id.widget_top_fill,
                        fillHeightDp,
                        TypedValue.COMPLEX_UNIT_DIP,
                    )
                    views.setViewLayoutHeight(
                        R.id.widget_bottom_fill,
                        fillHeightDp,
                        TypedValue.COMPLEX_UNIT_DIP,
                    )
                    views.setViewVisibility(R.id.widget_top_fill, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_bottom_fill, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_background_image_fit, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_background_image_crop, View.GONE)
                } else {
                    views.setImageViewBitmap(
                        R.id.widget_background_image_crop,
                        background.bitmap,
                    )
                    views.setViewVisibility(R.id.widget_top_fill, View.GONE)
                    views.setViewVisibility(R.id.widget_bottom_fill, View.GONE)
                    views.setViewVisibility(R.id.widget_background_image_fit, View.GONE)
                    views.setViewVisibility(R.id.widget_background_image_crop, View.VISIBLE)
                }
                views.setViewVisibility(R.id.widget_scrim, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_top_fill, View.GONE)
                views.setViewVisibility(R.id.widget_bottom_fill, View.GONE)
                views.setViewVisibility(R.id.widget_background_image_fit, View.GONE)
                views.setViewVisibility(R.id.widget_background_image_crop, View.GONE)
                views.setViewVisibility(R.id.widget_scrim, View.GONE)
            }

            if (counter == null) {
                views.setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
                views.setViewVisibility(R.id.widget_content, View.GONE)
                views.setTextViewText(R.id.widget_hint, context.getString(R.string.widget_empty))
                views.setViewVisibility(R.id.widget_hint, View.VISIBLE)
                return views
            }

            val breakdown = elapsedBreakdown(counter.startEpochMillis, nowMillis)

            views.setTextViewText(R.id.widget_title, counter.title)
            views.setTextViewText(R.id.widget_days, breakdown.days.toString().padStart(2, '0') + ":")
            views.setTextViewText(R.id.widget_hours, breakdown.hours.toString().padStart(2, '0') + ":")
            views.setChronometer(
                R.id.widget_chronometer,
                minuteChronometerBase(counter.startEpochMillis, nowMillis, elapsedRealtimeMillis),
                null,
                true,
            )
            views.setViewVisibility(R.id.widget_content, View.VISIBLE)
            views.setViewVisibility(R.id.widget_hint, View.GONE)
            return views
        }
    }
}
