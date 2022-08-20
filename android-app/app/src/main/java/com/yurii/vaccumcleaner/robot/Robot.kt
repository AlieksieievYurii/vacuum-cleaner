package com.yurii.vaccumcleaner.robot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.yurii.vaccumcleaner.requesthandler.RequestHandler

@JsonClass(generateAdapter = true)
data class MotorRequestModule(@Json(name = "motor_name") val motorName: String, val value: Int)

class Robot(private val requestHandler: RequestHandler) {

    suspend fun getSysInfo(): GeneralSystemInfo {
        return requestHandler.send("/get-sys-info", null, GeneralSystemInfo::class.java)!!
    }

    suspend fun walkForward(speedCmPerMinute: Int) {
        //TODO
    }

    suspend fun walkBackward(speedCmPerMinute: Int) {
        //TODO
    }

    suspend fun setMainBrushMotor(value: Int) = setMotor("main_brush", value)

    private suspend fun setMotor(motorName: String, value: Int) {
        requestHandler.send<Any>("/set-motor", MotorRequestModule(motorName, value), null)
    }
}