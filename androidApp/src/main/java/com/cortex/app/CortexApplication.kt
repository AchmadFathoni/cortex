package com.cortex.app

import android.app.Application
import com.cortex.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.plugin.module.dsl.startKoin

class CortexApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<AppModule> {
            androidLogger()
            androidContext(this@CortexApplication)
        }
    }
}
