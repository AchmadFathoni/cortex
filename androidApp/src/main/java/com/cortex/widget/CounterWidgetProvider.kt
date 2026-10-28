package com.cortex.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.cortex.R
import com.cortex.app.MainActivity
import com.cortex.core.chronometerBase
import com.cortex.core.elapsedBreakdown
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
        MidnightRefreshReceiver.scheduleNext(context)
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
            val now = nowMillis()
            val elapsedRealtime = SystemClock.elapsedRealtime()
            for (appWidgetId in appWidgetIds) {
                manager.updateAppWidget(
                    appWidgetId,
                    buildViews(context, appWidgetId, counter, now, elapsedRealtime),
                )
            }
        }

        private fun buildViews(
            context: Context,
            appWidgetId: Int,
            counter: CounterEntity?,
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

            if (counter == null) {
                views.setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
                views.setTextViewText(R.id.widget_days, "—")
                views.setTextViewText(R.id.widget_days_label, "")
                views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                views.setTextViewText(R.id.widget_hint, context.getString(R.string.widget_empty))
                views.setViewVisibility(R.id.widget_hint, View.VISIBLE)
                return views
            }

            val breakdown = elapsedBreakdown(counter.startEpochMillis, nowMillis)
            views.setTextViewText(R.id.widget_title, counter.title)
            views.setTextViewText(R.id.widget_days, breakdown.days.toString())
            views.setTextViewText(
                R.id.widget_days_label,
                if (breakdown.days == 1L) "day" else "days",
            )
            views.setChronometer(
                R.id.widget_chronometer,
                chronometerBase(counter.startEpochMillis, nowMillis, elapsedRealtimeMillis),
                null,
                true,
            )
            views.setViewVisibility(R.id.widget_chronometer, View.VISIBLE)
            views.setViewVisibility(R.id.widget_hint, View.GONE)
            return views
        }
    }
}
