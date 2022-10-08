package com.yurii.vaccumcleaner.screens.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.CleaningStatusEnum
import com.yurii.vaccumcleaner.robot.Robot
import com.yurii.vaccumcleaner.screens.panel.PanelViewModel
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

    private val _isPaused: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    private val _algorithmName: MutableStateFlow<String> = MutableStateFlow("")
    val algorithmName = _algorithmName.asStateFlow()

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
        netWorkScope.launch {
            val cleaningStatus = robot.getCleaningStatus()
            _algorithmName.value = cleaningStatus.requireCleaningInfo().algorithmName
            _isPaused.value = cleaningStatus.status == CleaningStatusEnum.PAUSED
        }
    }

    fun pauseOrResume() {
        netWorkScope.launch {
            if (isPaused.value) {
                robot.resumeCleaning()
                _isPaused.value = false
            } else {
                robot.pauseCleaning()
                _isPaused.value = true
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