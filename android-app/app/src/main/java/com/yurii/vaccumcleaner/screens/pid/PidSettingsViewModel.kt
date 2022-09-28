package com.yurii.vaccumcleaner.screens.pid

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.PidSettings
import com.yurii.vaccumcleaner.robot.Robot
import com.yurii.vaccumcleaner.utils.Empty
import com.yurii.vaccumcleaner.utils.value
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.IllegalStateException

class PidSettingsViewModel(private val robot: Robot) : ViewModel() {
    sealed class Event {
        data class ShowError(val exception: Throwable) : Event()
    }

    val proportional = ObservableField(String.Empty)
    val integral = ObservableField(String.Empty)
    val derivative = ObservableField(String.Empty)

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    private val errorHandler = CoroutineExceptionHandler { _, error -> sendEvent(Event.ShowError(error)) }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + errorHandler)

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        netWorkScope.launch {
            _isLoading.value = true
            val pidSettings = robot.getCurrentPidSettings()
            proportional.set(pidSettings.proportional.toString())
            integral.set(pidSettings.integral.toString())
            derivative.set(pidSettings.derivative.toString())
            _isLoading.value = false
        }
    }

    fun apply() {
        netWorkScope.launch {
            _isLoading.value = true
            val pidSettings = PidSettings(
                proportional = proportional.value.toFloat(),
                integral = integral.value.toFloat(),
                derivative = derivative.value.toFloat(),
            )
            robot.setPidSettings(pidSettings)
            _isLoading.value = false
        }
    }

    private fun sendEvent(event: Event) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val robot: Robot) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PidSettingsViewModel::class.java))
                return PidSettingsViewModel(robot) as T
            throw IllegalStateException("Given the model class is not assignable from PanelViewModel class")
        }
    }
}