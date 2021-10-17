package com.yurii.vaccumcleaner

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TestPacket(val id: String)

@JsonClass(generateAdapter = true)
data class WifiSettings(
    val ssid: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class SetWifiResponse(
    @Json(name = "is_connected") val isConnected: Boolean,
    val ip: String?,
    @Json(name = "error_message") val errorMessage: String?
)