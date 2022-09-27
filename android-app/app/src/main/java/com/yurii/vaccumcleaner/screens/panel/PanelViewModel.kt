package com.yurii.vaccumcleaner.screens.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

import java.lang.IllegalStateException

class PanelViewModel(private val robot: Robot) : ViewModel() {
    sealed class BatteryState {
        object Charging : BatteryState()
        object Charged : BatteryState()
        data class Working(val capacity: Int, val voltage: Float) : BatteryState()
    }

    sealed class Event {
        object NavigateToControlFragment : Event()
    }

    private val _batteryState: MutableStateFlow<BatteryState> = MutableStateFlow(BatteryState.Working(100, 16.7f))
    val batteryState = _batteryState.asStateFlow()

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    init {
        startReadingRobotState()
    }

    fun openManualControlFragment() {
        viewModelScope.launch {
            // _event.emit(Event.NavigateToControlFragment)
        }
    }

    fun openCleaningHistory() {

    }

    fun openPidSettings() {

    }

    fun openWifiSettings() {

    }

    fun openCleaningAlgoSettings() {

    }

    fun startCleaning() {

    }

    private fun startReadingRobotState() = viewModelScope.launch {
        while (true) {
            val robotData = robot.getRobotInputData()
            Timber.e(robotData.chargingState.toString())
            _batteryState.value = when (robotData.chargingState) {
                0 -> BatteryState.Working(robotData.batteryCapacity, robotData.batteryVoltage)
                1 -> BatteryState.Charging
                2 -> BatteryState.Charged
                else -> throw IllegalStateException("Unhandled battery state ID '${robotData.chargingState}'")
            }
            delay(1000)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val robot: Robot) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PanelViewModel::class.java))
                return PanelViewModel(robot) as T
            throw IllegalStateException("Given the model class is not assignable from PanelViewModel class")
        }
    }
}