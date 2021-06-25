package com.ht117.sandsara

import android.app.Application
import com.ht117.sandsara.di.appDI
import com.ht117.sandsara.di.dataDI
import com.ht117.sandsara.di.repoDI
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class App: Application() {

    /**
     * Initialize application with koin for dependencies injection
     * Setup Timber
     */
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            androidLogger(Level.DEBUG)
            modules(appDI, repoDI, dataDI)
        }

        if (BuildConfig.DEBUG) {
            System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(Reporter())
        }
    }
}