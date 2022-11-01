package com.yurii.vaccumcleaner.screens.wifi

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Robot
import com.yurii.vaccumcleaner.robot.WifiSettingsRequestModel
import com.yurii.vaccumcleaner.utils.Empty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


class WifiSettingsViewModel(private val robot: Robot) : ViewModel() {
    sealed class Event {
        data class ShowError(val error: Throwable) : Event()
        data class NavigateToWifiSetupDoneScreen(val deviceIpAddress: String) : Event()
    }

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    private val _availableAccessPoints: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val availableAccessPoints = _availableAccessPoints.asStateFlow()

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
            _availableAccessPoints.value = robot.getNetworkScan().availableAccessPoints.map { it.ssid }
            val currentWpaConfig = robot.getCurrentWpaConfig()
            ssidField.set(currentWpaConfig.ssid)
            passwordField.set(currentWpaConfig.password)
            _isLoading.value = false
        }
    }

    fun applyWifiSettings() {
        netWorkScope.launch {
            _isLoading.value = true
            val networkInfo = robot.setWifiSettings(
                WifiSettingsRequestModel(ssid = ssidField.get() ?: String.Empty, password = passwordField.get() ?: String.Empty)
            )
            _isLoading.value = false
            sendEvent(Event.NavigateToWifiSetupDoneScreen(networkInfo.ipAddress))
        }
    }

    class Factory(private val robot: Robot) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WifiSettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WifiSettingsViewModel(robot) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}