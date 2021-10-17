package com.yurii.vaccumcleaner

import com.yurii.vaccumcleaner.service.Service
import java.util.*

class ServiceWrapper(private val service: Service) {
    suspend fun performValidationRequest() {
        val testPacket = TestPacket(id = UUID.randomUUID().toString())
        val resp = service.request("send_test_packet", testPacket, TestPacket::class.java, TestPacket::class.java)
        if (resp.id != testPacket.id)
            throw Exception("Something went wrong!")
    }

    suspend fun getCurrentWifiSettings(): WifiSettings {
        return service.request("get_wifi_settings", WifiSettings::class.java)
    }

    suspend fun setWifiSettings(wifiSettings: WifiSettings): SetWifiResponse {
        return service.request("set_wifi_settings", wifiSettings, SetWifiResponse::class.java, WifiSettings::class.java, timeout = 13000)
    }
}