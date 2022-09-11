package com.yurii.vaccumcleaner.robot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeneralSystemInfo(
    val platform: String,
    @Json(name = "platform_release") val platformRelease: String,
    @Json(name = "platform_version") val platformVersion: String,
    val architecture: String,
    val hostname: String,
    @Json(name = "ip_address") val ipAddress: String,
    @Json(name = "mac_address") val macAddress: String,
    val processor: String
)

@JsonClass(generateAdapter = true)
data class RobotInputData(
    @Json(name = "el") val leftBumperHit: Boolean,
    @Json(name = "er") val rightBumperHit: Boolean,
    @Json(name = "eb") val isDustBoxInserted: Boolean,
    @Json(name = "ec") val isLidClosed: Boolean,
    @Json(name = "lrs") val leftDistanceRange: Int,
    @Json(name = "crs") val centerDistanceRange: Int,
    @Json(name = "rrs") val rightDistanceRange: Int,
    @Json(name = "bv") val batteryVoltage: Float,
    @Json(name = "bc") val batteryCapacity: Int,
)