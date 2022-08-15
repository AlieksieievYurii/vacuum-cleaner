package com.yurii.vaccumcleaner.robot

import com.yurii.vaccumcleaner.requesthandler.RequestHandler

class Robot(private val requestHandler: RequestHandler) {

    suspend fun getSysInfo(): GeneralSystemInfo {
        return requestHandler.send("/get-sys-info", null, GeneralSystemInfo::class.java)
    }

}