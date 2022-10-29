package com.yurii.vaccumcleaner.robot
import com.yurii.vaccumcleaner.utils.requesthandler.RequestHandler

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
        return requestHandler.send("/get-current-pid", null, PidSettings::class.java)!!
    }

    override suspend fun startCleaning() = manageCleaningProcess("start")

    override suspend fun pauseCleaning() = manageCleaningProcess("pause")

    override suspend fun resumeCleaning() = manageCleaningProcess("resume")

    override suspend fun stopCleaning() = manageCleaningProcess("stop")

    private suspend fun manageCleaningProcess(command: String) {
        requestHandler.send<Any>("/manage-cleaning", ManageCleaningExecution(command), null)
    }

    override suspend fun getCleaningStatus(): CleaningStatus {
        return requestHandler.send("/get-cleaning-status", null, CleaningStatus::class.java)!!
    }

    override suspend fun shutDown() {
        requestHandler.send<Any>("/power", PowerCommand(Power.SHUT_DOWN), null)
    }

    override suspend fun reboot() {
        requestHandler.send<Any>("/power", PowerCommand(Power.REBOOT), null)
    }

    override suspend fun getCurrentWpaConfig(): WpaConfig {
        return requestHandler.send("/get-current-wifi-credentials", null, WpaConfig::class.java)!!
    }

    override suspend fun setPidSettings(pidSettings: PidSettings) {
        requestHandler.send<Any>("/set-current-pid", pidSettings, null)
    }

    override suspend fun getAlgorithms(): AlgorithmList {
        return requestHandler.send("/get-algorithms", null, AlgorithmList::class.java)!!
    }

    override suspend fun setAlgorithm(algorithm: Algorithm) {
        requestHandler.send<Any>("/set-algorithm", algorithm, null)
    }

    private suspend fun setMotor(motorName: String, value: Int) {
        requestHandler.send<Any>("/set-motor", MotorRequestModule(motorName, value), null)
    }

    private suspend fun walk(direction: String, speedCmPerMinute: Int) {
        requestHandler.send<Any>("/movement", MovementRequestModel(direction, speedCmPerMinute), null)
    }
}