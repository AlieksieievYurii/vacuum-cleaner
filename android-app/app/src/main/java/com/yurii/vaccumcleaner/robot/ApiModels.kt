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