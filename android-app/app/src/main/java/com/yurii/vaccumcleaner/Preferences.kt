package com.yurii.vaccumcleaner

import android.content.Context

object Preferences {
    private const val SP_KEY_ROBOT_IP = "com.yurii.vaccumcleaner.sp_default.robot_ip"
    private const val SP_DEFAULT = "com.yurii.vaccumcleaner.sp_default"

    fun saveRobotIpAddress(context: Context, ip: String) {
        val storage = context.getSharedPreferences(SP_DEFAULT, Context.MODE_PRIVATE)
        with(storage.edit()) {
            putString(SP_KEY_ROBOT_IP, ip)
            apply()
        }
    }

    fun getRobotIpAddress(context: Context): String? {
        val storage = context.getSharedPreferences(SP_DEFAULT, Context.MODE_PRIVATE)
        return storage.getString(SP_KEY_ROBOT_IP, null)
    }

}