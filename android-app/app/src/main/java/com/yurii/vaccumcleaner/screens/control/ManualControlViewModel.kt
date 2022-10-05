package com.yurii.vaccumcleaner.screens.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Robot
import com.yurii.vaccumcleaner.robot.RobotInputData
import com.yurii.vaccumcleaner.screens.panel.widgets.HeaderWidget
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ManualControlViewModel(private val robot: Robot) : ViewModel() {
    sealed class Event {
        data class ShowError(val exception: Throwable) : Event()
    }

    private val _runTimeRobotData: MutableStateFlow<RobotInputData?> = MutableStateFlow(null)
    val runTimeRobotData = _runTimeRobotData.asStateFlow()

    private val _batteryState: MutableStateFlow<HeaderWidget.BatteryState> = MutableStateFlow(HeaderWidget.BatteryState.Working(100, 16.7f))
    val batteryState = _batteryState.asStateFlow()

    var wheelSpeed = 0
    var withBreak = false

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch {
            _event.emit(Event.ShowError(exception))
        }
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + errorHandler)

    init {
        startReadingAndHandlingRobotInputData()
    }

    private fun startReadingAndHandlingRobotInputData() {
        netWorkScope.launch {
            while (true) {
                val date = robot.getRobotInputData()
                _runTimeRobotData.value = date

                _batteryState.value = when (date.chargingState) {
                    0 -> HeaderWidget.BatteryState.Working(date.batteryCapacity, date.batteryVoltage)
                    1 -> HeaderWidget.BatteryState.Charging
                    2 -> HeaderWidget.BatteryState.Charged
                    else -> throw java.lang.IllegalStateException("Unhandled battery state ID '${date.chargingState}'")
                }
                delay(500)
            }
        }
    }

    fun moveForward() {
        netWorkScope.launch { robot.walkForward(wheelSpeed) }
    }

    fun moveBackward() {
        netWorkScope.launch { robot.walkBackward(wheelSpeed) }
    }

    fun turnLeft() {
        netWorkScope.launch { robot.rotateLeft(wheelSpeed) }
    }

    fun turnRight() {
        netWorkScope.launch { robot.rotateRight(wheelSpeed) }
    }

    fun stop() {
        netWorkScope.launch { robot.stopMovement(withBreak) }
    }

    fun setVacuumMotorSpeed(speedInPercentage: Int) {
        netWorkScope.launch { robot.setVacuumMotor(speedInPercentage) }
    }

    fun setMainBrushMotorSpeed(speedInPercentage: Int) {
        netWorkScope.launch { robot.setMainBrushMotor(speedInPercentage) }
    }

    fun setRightBrushSpeed(speedInPercentage: Int) {
        netWorkScope.launch { robot.setRightBrushMotor(speedInPercentage) }
    }

    fun setLeftBrushSpeed(speedInPercentage: Int) {
        netWorkScope.launch { robot.setLeftBrushMotor(speedInPercentage) }
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