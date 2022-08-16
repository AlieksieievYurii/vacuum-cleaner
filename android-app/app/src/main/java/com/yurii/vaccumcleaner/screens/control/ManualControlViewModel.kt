package com.yurii.vaccumcleaner.screens.control

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.requesthandler.Communicator
import com.yurii.vaccumcleaner.requesthandler.RequestHandler
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Range
import timber.log.Timber

class ManualControlViewModel(communicator: Communicator) : ViewModel() {
    private val robot = Robot(RequestHandler(communicator, viewModelScope).also {
        Log.i("TEST", "OK")
        it.start() })

    init {
        viewModelScope.launch {
            val r = robot.getSysInfo()
            Timber.d(r.toString())
        }
    }

    fun moveForward() {

    }

    fun moveBackward() {

    }

    fun turnLeft() {

    }

    fun turnRight() {

    }

    fun stop() {

    }

    fun setWheelSpeed(cmPerMinute: Int) {
        Log.i("setWheelSpeed", cmPerMinute.toString())
    }

    fun setVacuumMotorSpeed(speedInPercentage: Int) {
        Log.i("setVacuumMotorSpeed", speedInPercentage.toString())
    }

    fun setMainBrushMotorSpeed(speedInPercentage: Int) {
        Log.i("setMainBrushMotorSpeed", speedInPercentage.toString())
    }

    fun setRightBrushSpeed(speedInPercentage: Int) {
        Log.i("setRightBrushSpeed", speedInPercentage.toString())
    }

    fun setLeftBrushSpeed(speedInPercentage: Int) {
        Log.i("setLeftBrushSpeed", speedInPercentage.toString())
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val communicator: Communicator) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManualControlViewModel::class.java))
                return ManualControlViewModel(communicator) as T
            throw IllegalStateException("Given the model class is not assignable from ManualControlViewModel class")
        }
    }
}