package com.yurii.vaccumcleaner.robot

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
            chargingState = 0
        )
    }
}