package com.cortex.di

import android.content.Context
import com.cortex.core.CounterRepo
import com.cortex.core.CounterStorage
import com.cortex.core.SharedCounterRepo
import com.cortex.core.WidgetUpdater
import com.cortex.data.database.AppDatabase
import com.cortex.data.database.CounterDao
import com.cortex.data.storage.RoomCounterStorage
import com.cortex.ui.di.UIModules
import com.cortex.widget.getDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [UIModules::class])
@ComponentScan("com.cortex")
class AppModule {
    @Single
    fun provideDatabase(context: Context): AppDatabase = getDatabase(context.applicationContext)

    @Single
    fun provideCounterDao(db: AppDatabase): CounterDao = db.counterDao()

    @Single(binds = [CounterStorage::class])
    fun provideCounterStorage(dao: CounterDao): CounterStorage = RoomCounterStorage(dao)

    @Single(binds = [CounterRepo::class])
    fun provideCounterRepo(
        storage: CounterStorage,
        widgetUpdater: WidgetUpdater,
    ): CounterRepo = SharedCounterRepo(storage, widgetUpdater)
}
