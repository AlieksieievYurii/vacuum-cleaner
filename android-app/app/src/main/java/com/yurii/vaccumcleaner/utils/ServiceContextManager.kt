package com.yurii.vaccumcleaner.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.lang.UnsupportedOperationException

class ServiceContextManager(private val context: Context) {
    private val wifiManager: WifiManager? = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private fun getWifiManager() = wifiManager ?: throw UnsupportedOperationException("Wifi Service is not supported")

    fun isWifiEnabled() = getWifiManager().isWifiEnabled
}