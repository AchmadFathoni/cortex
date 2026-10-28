package com.cortex.ui.di

import com.cortex.core.WidgetBackgroundController

class NoopWidgetBackgroundController : WidgetBackgroundController {
    override fun chooseBackground() {}
    override fun clearBackground() {}
}
