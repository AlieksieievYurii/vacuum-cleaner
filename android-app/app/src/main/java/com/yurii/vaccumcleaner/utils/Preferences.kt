package com.yurii.vaccumcleaner.utils

import android.content.Context

class Preferences(private val context: Context) {
    fun saveRobotIpAddress(ip: String) {
        val storage = context.getSharedPreferences(SP_DEFAULT, Context.MODE_PRIVATE)
        with(storage.edit()) {
            putString(SP_KEY_ROBOT_IP, ip)
            apply()
        }
    }

    fun getRobotIpAddress(): String? {
        val storage = context.getSharedPreferences(SP_DEFAULT, Context.MODE_PRIVATE)
        return storage.getString(SP_KEY_ROBOT_IP, null)
    }

    companion object {
        private const val SP_KEY_ROBOT_IP = "com.yurii.vaccumcleaner.sp_default.robot_ip"
        private const val SP_DEFAULT = "com.yurii.vaccumcleaner.sp_default"
    }
}