package com.yurii.vaccumcleaner.screens.control

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class ManualControlViewModel(private val robot: Robot) : ViewModel() {

    private val _isDustBoxInserted: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isDustBoxInserted = _isDustBoxInserted.asStateFlow()

    private val _isLidClosed: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLidClosed = _isLidClosed.asStateFlow()

    var wheelSpeed = 0
    var withBreak = false

    init {
        startReadingAndHandlingRobotInputData()
    }

    private fun startReadingAndHandlingRobotInputData() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val data = robot.getRobotInputData()
                _isDustBoxInserted.value = data.isDustBoxInserted
                _isLidClosed.value = data.isLidClosed
                delay(100)
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
    class Factory(private val robot: Robot) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManualControlViewModel::class.java))
                return ManualControlViewModel(robot) as T
            throw IllegalStateException("Given the model class is not assignable from ManualControlViewModel class")
        }
    }
}