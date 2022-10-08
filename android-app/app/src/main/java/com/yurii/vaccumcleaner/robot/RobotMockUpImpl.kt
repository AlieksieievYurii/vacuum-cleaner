package com.yurii.vaccumcleaner.robot

import kotlinx.coroutines.delay
import timber.log.Timber

class RobotMockUpImpl : Robot {
    override suspend fun walkForward(speedCmPerMinute: Int) {
        Timber.i("Walk Forward is triggered. Speed: $speedCmPerMinute CM/Minute")
    }

    override suspend fun walkBackward(speedCmPerMinute: Int) {
        Timber.i("Walk Backward is triggered. Speed: $speedCmPerMinute CM/Minute")
    }

    override suspend fun rotateLeft(speedCmPerMinute: Int) {
        Timber.i("Left Rotation is triggered. Speed: $speedCmPerMinute CM/Minute")
    }

    override suspend fun rotateRight(speedCmPerMinute: Int) {
        Timber.i("Right Rotation is triggered. Speed: $speedCmPerMinute CM/Minute")
    }

    override suspend fun stopMovement(withBreak: Boolean) {
        Timber.i("Stop Movement is triggered. With break: $withBreak")
    }

    override suspend fun setVacuumMotor(value: Int) {
        Timber.i("Set Vacuum Motor Speed: $value %")
    }

    override suspend fun setMainBrushMotor(value: Int) {
        Timber.i("Set Main Brush Motor Speed: $value %")
    }

    override suspend fun setLeftBrushMotor(value: Int) {
        Timber.i("Set Left Brush Motor Speed: $value %")
    }

    override suspend fun setRightBrushMotor(value: Int) {
        Timber.i("Set Right Brush Motor Speed: $value %")
    }

    override suspend fun getRobotInputData(): RobotInputData {
        return RobotInputData(
            leftBumperHit = false,
            rightBumperHit = false,
            isDustBoxInserted = false,
            isLidClosed = true,
            leftDistanceRange = 200,
            centerDistanceRange = 200,
            rightDistanceRange = 100,
            batteryVoltage = 16.4f,
            batteryCapacity = 100,
            chargingState = 0,
            leftWheelSpeed = 1000,
            rightWheelSpeed = 1000
        )
    }

    override suspend fun getCurrentPidSettings(): PidSettings {
        delay(1000)
        return PidSettings(0.1f, 0.03f, 0.0f)
    }

    override suspend fun setPidSettings(pidSettings: PidSettings) {
        delay(1000)
        Timber.i("Setting up PID: $pidSettings")
    }

    override suspend fun getAlgorithms(): AlgorithmList {
        TODO("Not yet implemented")
    }

    override suspend fun setAlgorithm(algorithm: Algorithm) {
        Timber.i("Setting algorithm $algorithm")
    }

    override suspend fun startCleaning() {
        Timber.i("Start cleaning...")
    }

    override suspend fun pauseCleaning() {
        Timber.i("Pause cleaning...")
    }

    override suspend fun resumeCleaning() {
        Timber.i("Resume cleaning...")
    }

    override suspend fun stopCleaning() {
        Timber.i("Stop cleaning...")
    }

    override suspend fun getCleaningStatus(): CleaningStatus {
        return CleaningStatus(CleaningStatusEnum.RUNNING, CleaningExecutionInfo("simple", "12:00"))
    }

    override suspend fun shutDown() {
        Timber.i("Shut down")
    }

    override suspend fun reboot() {
        Timber.i("Reboot")
    }
}