package com.cortex.widget

import android.content.Context
import com.cortex.core.WidgetBackgroundMode

object WidgetBackgroundSettings {
    private const val PREFS = "cortex_widget"
    private const val KEY_MODE = "background_mode"

    fun mode(context: Context): WidgetBackgroundMode {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_MODE, null)
        return WidgetBackgroundMode.entries.firstOrNull { it.name == stored }
            ?: WidgetBackgroundMode.FIT_CENTER
    }

    fun setMode(context: Context, mode: WidgetBackgroundMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.name)
            .apply()
    }
}
