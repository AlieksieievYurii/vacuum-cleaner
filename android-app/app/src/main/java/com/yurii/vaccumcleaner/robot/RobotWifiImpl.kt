package com.yurii.vaccumcleaner.robot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.yurii.vaccumcleaner.utils.requesthandler.RequestHandler

@JsonClass(generateAdapter = true)
data class MotorRequestModule(@Json(name = "motor_name") val motorName: String, val value: Int)

@JsonClass(generateAdapter = true)
data class MovementRequestModel(val direction: String, val speed: Int)

@JsonClass(generateAdapter = true)
data class StopMovementRequestModel(@Json(name = "with_break") val withBreak: Boolean)

class RobotWifiImplementation(private val requestHandler: RequestHandler) : Robot {

    suspend fun getSysInfo(): GeneralSystemInfo {
        return requestHandler.send("/get-sys-info", null, GeneralSystemInfo::class.java)!!
    }

    override suspend fun walkForward(speedCmPerMinute: Int) = walk("forward", speedCmPerMinute)

    override suspend fun walkBackward(speedCmPerMinute: Int) = walk("backward", speedCmPerMinute)

    override suspend fun rotateLeft(speedCmPerMinute: Int) = walk("left", speedCmPerMinute)

    override suspend fun rotateRight(speedCmPerMinute: Int) = walk("right", speedCmPerMinute)

    override suspend fun stopMovement(withBreak: Boolean) {
        requestHandler.send<Any>("/stop-movement", StopMovementRequestModel(withBreak), null)
    }

    override suspend fun setVacuumMotor(value: Int) = setMotor("vacuum", value)

    override suspend fun setMainBrushMotor(value: Int) = setMotor("main_brush", value)

    override suspend fun setLeftBrushMotor(value: Int) = setMotor("left_brush", value)

    override suspend fun setRightBrushMotor(value: Int) = setMotor("right_brush", value)

    override suspend fun getRobotInputData() = requestHandler.send("/get-a1-data", null, RobotInputData::class.java)!!

    override suspend fun getCurrentPidSettings(): PidSettings {
        TODO("Not yet implemented")
    }

    override suspend fun setPidSettings(pidSettings: PidSettings) {
        TODO("Not yet implemented")
    }

    private suspend fun setMotor(motorName: String, value: Int) {
        requestHandler.send<Any>("/set-motor", MotorRequestModule(motorName, value), null)
    }

    private suspend fun walk(direction: String, speedCmPerMinute: Int) {
        requestHandler.send<Any>("/movement", MovementRequestModel(direction, speedCmPerMinute), null)
    }
}