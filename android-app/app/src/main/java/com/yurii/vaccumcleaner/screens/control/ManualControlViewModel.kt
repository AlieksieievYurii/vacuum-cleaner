package com.yurii.vaccumcleaner.screens.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

class ManualControlViewModel(private val robot: Robot) : ViewModel() {
    var wheelSpeed = 0
    var withBreak = false

    init {
        viewModelScope.launch {
            (0..10).forEach {
                async {
                    val r = robot.getSysInfo()
                    Timber.d(r.toString())
                }
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