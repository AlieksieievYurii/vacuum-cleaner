package com.yurii.vaccumcleaner.screens.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.*
import com.yurii.vaccumcleaner.screens.panel.widgets.HeaderWidget
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.IllegalStateException

class CleaningExecutionViewModel(private val robot: Robot) : ViewModel() {
    sealed class Event {
        object NavigateToPanelFragment : Event()
        data class ShowError(val exception: Throwable) : Event()
    }

    private val _cleaningStatus: MutableStateFlow<CleaningStatus?> = MutableStateFlow(null)
    val cleaningStatus = _cleaningStatus.asStateFlow()

    private val _batteryState: MutableStateFlow<HeaderWidget.BatteryState> = MutableStateFlow(HeaderWidget.BatteryState.Working(100, 16.7f))
    val batteryState = _batteryState.asStateFlow()

    private val _lidIsOpen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val lidIsOpened = _lidIsOpen.asStateFlow()

    private val _dustBoxIsOut: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val dustBoxIsOut = _dustBoxIsOut.asStateFlow()

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch {
            _event.emit(Event.ShowError(exception))
        }
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + errorHandler)

    init {

        startReadingRobotState()
    }

    private fun startReadingRobotState() = netWorkScope.launch {
        refreshStates()
        _isLoading.value = false

        while (true) {
            refreshStates()
            delay(1000)
        }
    }

    private suspend fun refreshStates() {
        val cleaningStatus = robot.getCleaningStatus()
        val robotData = robot.getRobotInputData()
        _cleaningStatus.value = cleaningStatus

        _batteryState.value = when (robotData.chargingState) {
            0 -> HeaderWidget.BatteryState.Working(robotData.batteryCapacity, robotData.batteryVoltage)
            1 -> HeaderWidget.BatteryState.Charging
            2 -> HeaderWidget.BatteryState.Charged
            else -> throw IllegalStateException("Unhandled battery state ID '${robotData.chargingState}'")
        }

        _lidIsOpen.value = !robotData.isLidClosed
        _dustBoxIsOut.value = !robotData.isDustBoxInserted
    }

    fun pauseOrResume() {
        netWorkScope.launch {
            _cleaningStatus.value?.run {
                _isLoading.value = true
                if (status == CleaningStatusEnum.PAUSED) {
                    robot.resumeCleaning()
                } else {
                    robot.pauseCleaning()
                }
                refreshStates()
                delay(500)
                _isLoading.value = false
            }
        }
    }

    fun stopCleaning() {
        netWorkScope.launch {
            robot.stopCleaning()
            _event.emit(Event.NavigateToPanelFragment)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val robot: Robot) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CleaningExecutionViewModel::class.java))
                return CleaningExecutionViewModel(robot) as T
            throw IllegalStateException("Given the model class is not assignable from CleaningExecutionViewModel class")
        }
    }
}