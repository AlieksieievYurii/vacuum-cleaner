package com.yurii.vaccumcleaner.screens.wifi

import android.bluetooth.BluetoothDevice
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.Empty
import com.yurii.vaccumcleaner.SetWifiResponse
import com.yurii.vaccumcleaner.WifiSettings
import com.yurii.vaccumcleaner.service.Service
import com.yurii.vaccumcleaner.value
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception


class WifiSettingsViewModel(bluetoothDevice: BluetoothDevice) : ViewModel() {
    sealed class State {
        object None : State()
        object LoadingWifiSettings : State()
        object ApplyingWifiSettings : State()
        data class Error(val message: String) : State()
    }

    sealed class Event {
        object CurrentSetupHasBeenReturned : Event()
        object SettingsHaveBeenApplied : Event()
    }

    val ssidField = ObservableField(String.Empty)
    val passwordField = ObservableField(String.Empty)

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.LoadingWifiSettings)
    val state = _state.asStateFlow()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val service = Service(viewModelScope, bluetoothDevice, listOf())

    init {
        viewModelScope.launch {
            service.start()
            try {
                val wifiSettings = service.request("get_wifi_settings", WifiSettings::class.java)
                _state.value = State.None
                ssidField.set(wifiSettings.ssid)
                passwordField.set(wifiSettings.password)
            } catch (error: Exception) {
                _state.value = State.Error(error.message ?: "No error message!")
            }
        }
    }

    fun applyWifiSettings() {
        viewModelScope.launch {
            _state.value = State.ApplyingWifiSettings
            val wifiSettings = WifiSettings(ssid = ssidField.value, password = passwordField.value)
            try {
                val resp = service.request(
                    "set_wifi_settings", wifiSettings, SetWifiResponse::class.java, WifiSettings::class.java,
                    timeout = 13000
                )
                if (resp.isConnected) {
                    _state.value = State.None
                    eventChannel.send(Event.SettingsHaveBeenApplied)
                }
            } catch (error: Exception) {
                _state.value = State.Error(error.message ?: "No error message!")
            }
        }
    }

    class Factory(private val bluetoothDevice: BluetoothDevice) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WifiSettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WifiSettingsViewModel(bluetoothDevice) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}