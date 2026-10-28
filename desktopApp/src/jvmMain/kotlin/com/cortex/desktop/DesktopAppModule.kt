package com.cortex.desktop

import com.cortex.core.CounterRepo
import com.cortex.core.CounterStorage
import com.cortex.core.SharedCounterRepo
import com.cortex.core.WidgetUpdater
import com.cortex.ui.di.DefaultWidgetUpdater
import com.cortex.ui.di.UIModules
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [UIModules::class])
@ComponentScan("com.cortex.desktop")
class DesktopAppModule {
    @Single(binds = [CounterStorage::class])
    fun provideCounterStorage(): CounterStorage = JsonCounterStorage()

    @Single(binds = [CounterRepo::class])
    fun provideCounterRepo(
        storage: CounterStorage,
        widgetUpdater: WidgetUpdater,
    ): CounterRepo = SharedCounterRepo(storage, widgetUpdater)

    @Single(binds = [WidgetUpdater::class])
    fun provideWidgetUpdater(): WidgetUpdater = DefaultWidgetUpdater()
}
