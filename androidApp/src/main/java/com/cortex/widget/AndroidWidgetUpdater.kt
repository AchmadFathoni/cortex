package com.cortex.widget

import android.content.Context
import com.cortex.core.WidgetUpdater
import org.koin.core.annotation.Single

@Single(binds = [WidgetUpdater::class])
class AndroidWidgetUpdater(private val context: Context) : WidgetUpdater {
    override fun notifyDataChanged() {
        CounterWidgetProvider.refreshAllAsync(context)
    }
}
