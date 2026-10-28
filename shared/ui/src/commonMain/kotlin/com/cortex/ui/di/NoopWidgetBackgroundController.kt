package com.cortex.ui.di

import com.cortex.core.WidgetBackgroundController
import com.cortex.core.WidgetBackgroundMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NoopWidgetBackgroundController : WidgetBackgroundController {
    private val _mode = MutableStateFlow(WidgetBackgroundMode.FIT_CENTER)

    override val mode: StateFlow<WidgetBackgroundMode> = _mode

    override fun chooseBackground() {}

    override fun clearBackground() {}

    override fun setBackgroundMode(mode: WidgetBackgroundMode) {
        _mode.value = mode
    }
}
