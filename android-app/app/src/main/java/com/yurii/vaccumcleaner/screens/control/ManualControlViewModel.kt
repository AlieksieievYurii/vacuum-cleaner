package com.yurii.vaccumcleaner.screens.control

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.requesthandler.Communicator
import com.yurii.vaccumcleaner.requesthandler.RequestHandler
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Range
import timber.log.Timber

class ManualControlViewModel(private val robot: Robot) : ViewModel() {

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
            robot.walkForward(2000)
        }
    }

    fun moveBackward() {
        viewModelScope.launch(Dispatchers.IO) {
            robot.walkBackward(2000)
        }
    }

    fun turnLeft() {
        viewModelScope.launch(Dispatchers.IO) {
            robot.rotateLeft(2000)
        }
    }

    fun turnRight() {
        viewModelScope.launch(Dispatchers.IO) {
            robot.rotateRight(2000)
        }
    }

    fun stop() {
        viewModelScope.launch(Dispatchers.IO) {
            robot.stopMovement(true)
        }
    }

    fun setWheelSpeed(cmPerMinute: Int) {
        Log.i("setWheelSpeed", cmPerMinute.toString())
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