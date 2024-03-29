package com.yurii.vaccumcleaner.screens.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Robot
import com.yurii.vaccumcleaner.screens.panel.widgets.HeaderWidget
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

import java.lang.IllegalStateException

class PanelViewModel(private val robot: Robot) : ViewModel() {
    sealed class Event {
        object NavigateToControlFragment : Event()
        object NavigateToPidSettingsFragment : Event()
        object NavigateToAlgorithmSetupFragment : Event()
        object NavigateToCleaningExecutionFragment : Event()
        data class ShowError(val exception: Throwable) : Event()
    }

    private val _batteryState: MutableStateFlow<HeaderWidget.BatteryState> = MutableStateFlow(HeaderWidget.BatteryState.Working(100, 16.7f))
    val batteryState = _batteryState.asStateFlow()

    private val _lidIsOpen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val lidIsOpened = _lidIsOpen.asStateFlow()

    private val _dustBoxIsOut: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val dustBoxIsOut = _dustBoxIsOut.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch {
            _isLoading.value = false
            _event.emit(Event.ShowError(exception))
        }
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + errorHandler)

    init {
        startReadingRobotState()
    }

    fun openManualControlFragment() {
        viewModelScope.launch {
            _event.emit(Event.NavigateToControlFragment)
        }
    }

    fun openCleaningHistory() {

    }

    fun openPidSettings() = sendEvent(Event.NavigateToPidSettingsFragment)

    fun openWifiSettings() {

    }

    fun shutDown() {
        netWorkScope.launch {
            _isLoading.emit(true)
            robot.shutDown()
            _isLoading.emit(false)
        }
    }

    fun reboot() {
        netWorkScope.launch {
            _isLoading.emit(true)
            robot.reboot()
            _isLoading.emit(false)
        }
    }

    fun openCleaningAlgoSettings() = sendEvent(Event.NavigateToAlgorithmSetupFragment)

    fun startCleaning() {
        netWorkScope.launch {
            _isLoading.value = true
            delay(1000)
            robot.startCleaning()
            _isLoading.value = false
            sendEvent(Event.NavigateToCleaningExecutionFragment)
        }
    }

    private fun sendEvent(event: Event) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    private fun startReadingRobotState() = netWorkScope.launch {
        while (true) {
            val robotData = robot.getRobotInputData()

            _batteryState.value = when (robotData.chargingState) {
                0 -> HeaderWidget.BatteryState.Working(robotData.batteryCapacity, robotData.batteryVoltage)
                1 -> HeaderWidget.BatteryState.Charging
                2 -> HeaderWidget.BatteryState.Charged
                else -> throw IllegalStateException("Unhandled battery state ID '${robotData.chargingState}'")
            }

            _lidIsOpen.value = !robotData.isLidClosed
            _dustBoxIsOut.value = !robotData.isDustBoxInserted

            delay(1000)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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