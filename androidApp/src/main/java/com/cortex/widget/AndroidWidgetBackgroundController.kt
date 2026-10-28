package com.cortex.widget

import android.content.Context
import android.net.Uri
import com.cortex.core.WidgetBackgroundController
import com.cortex.core.WidgetBackgroundMode
import com.cortex.core.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AndroidWidgetBackgroundController(
    private val context: Context,
    private val widgetUpdater: WidgetUpdater,
) : WidgetBackgroundController {
    var picker: (() -> Unit)? = null
    private val _mode = MutableStateFlow(WidgetBackgroundSettings.mode(context))

    override val mode: StateFlow<WidgetBackgroundMode> = _mode

    override fun chooseBackground() {
        picker?.invoke()
    }

    override fun clearBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            WidgetBackgroundStore.clear(context)
            widgetUpdater.notifyDataChanged()
        }
    }

    override fun setBackgroundMode(mode: WidgetBackgroundMode) {
        WidgetBackgroundSettings.setMode(context, mode)
        _mode.value = mode
        widgetUpdater.notifyDataChanged()
    }

    fun saveImage(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            WidgetBackgroundStore.save(context, uri)
            widgetUpdater.notifyDataChanged()
        }
    }
}
