package com.yurii.vaccumcleaner.screens.wifi

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.RobotWifiImplementation
import com.yurii.vaccumcleaner.utils.Empty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


class WifiSettingsViewModel(private val robot: RobotWifiImplementation) : ViewModel() {
    sealed class Event {
        data class ShowError(val error: Throwable) : Event()
    }

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    val ssidField = ObservableField(String.Empty)
    val passwordField = ObservableField(String.Empty)

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        _isLoading.value = false
        sendEvent(Event.ShowError(error))
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + errorHandler)

    private fun sendEvent(event: Event) = viewModelScope.launch {
        _event.emit(event)
    }

    init {
        netWorkScope.launch {
            _isLoading.value = true
            val currentWpaConfig = robot.getCurrentWpaConfig()
            ssidField.set(currentWpaConfig.ssid)
            passwordField.set(currentWpaConfig.password)
            _isLoading.value = false
        }
    }

    fun applyWifiSettings() {

    }

    class Factory(private val robot: RobotWifiImplementation) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WifiSettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WifiSettingsViewModel(robot) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}