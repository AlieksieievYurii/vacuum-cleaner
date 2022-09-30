package com.yurii.vaccumcleaner.screens.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.RobotWifiImplementation
import com.yurii.vaccumcleaner.robot.RobotInputData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ManualControlViewModel(private val robot: RobotWifiImplementation) : ViewModel() {
    private val _runTimeRobotData: MutableStateFlow<RobotInputData?> = MutableStateFlow(null)
    val runTimeRobotData = _runTimeRobotData.asStateFlow()

    var wheelSpeed = 0
    var withBreak = false

    init {
        startReadingAndHandlingRobotInputData()
    }

    private fun startReadingAndHandlingRobotInputData() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                _runTimeRobotData.value = robot.getRobotInputData()
                delay(500)
            }
        }
    }

    fun moveForward() {
        viewModelScope.launch(Dispatchers.IO) {
            robot.walkForward(wheelSpeed)
        }
    }

    fun moveBackward() {
        viewModelScope.launch(Dispatchers.IO) {
            robot.walkBackward(wheelSpeed)
        }
    }

    fun turnLeft() {
        viewModelScope.launch(Dispatchers.IO) {
            robot.rotateLeft(wheelSpeed)
        }
    }

    fun turnRight() {
        viewModelScope.launch(Dispatchers.IO) {
            robot.rotateRight(wheelSpeed)
        }
    }

    fun stop() {
        viewModelScope.launch(Dispatchers.IO) {
            robot.stopMovement(withBreak)
        }
    }

    fun setVacuumMotorSpeed(speedInPercentage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            robot.setVacuumMotor(speedInPercentage)
        }
    }

    fun setMainBrushMotorSpeed(speedInPercentage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            robot.setMainBrushMotor(speedInPercentage)
        }
    }

    fun setRightBrushSpeed(speedInPercentage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            robot.setRightBrushMotor(speedInPercentage)
        }
    }

    fun setLeftBrushSpeed(speedInPercentage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            robot.setLeftBrushMotor(speedInPercentage)
        }
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(private val robot: RobotWifiImplementation) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManualControlViewModel::class.java))
                return ManualControlViewModel(robot) as T
            throw IllegalStateException("Given the model class is not assignable from ManualControlViewModel class")
        }
    }
}