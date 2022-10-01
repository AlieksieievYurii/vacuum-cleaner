package com.yurii.vaccumcleaner.robot

interface Robot {
    suspend fun walkForward(speedCmPerMinute: Int)
    suspend fun walkBackward(speedCmPerMinute: Int)
    suspend fun rotateLeft(speedCmPerMinute: Int)
    suspend fun rotateRight(speedCmPerMinute: Int)
    suspend fun stopMovement(withBreak: Boolean)
    suspend fun setVacuumMotor(value: Int)
    suspend fun setMainBrushMotor(value: Int)
    suspend fun setLeftBrushMotor(value: Int)
    suspend fun setRightBrushMotor(value: Int)
    suspend fun getRobotInputData(): RobotInputData
    suspend fun getCurrentPidSettings(): PidSettings
    suspend fun setPidSettings(pidSettings: PidSettings)
    suspend fun getAlgorithmScripts(): AlgorithmScriptList
}