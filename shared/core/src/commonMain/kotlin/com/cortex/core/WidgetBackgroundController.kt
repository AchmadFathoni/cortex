package com.cortex.core

import kotlinx.coroutines.flow.StateFlow

interface WidgetBackgroundController {
    val mode: StateFlow<WidgetBackgroundMode>

    fun chooseBackground()
    fun clearBackground()
    fun setBackgroundMode(mode: WidgetBackgroundMode)
}
