package com.yurii.vaccumcleaner

import android.app.Application
import com.yurii.vaccumcleaner.robot.WifiCommunicator
import timber.log.Timber

class MyApplication : Application() {

    val wifiCommunicator = WifiCommunicator()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}