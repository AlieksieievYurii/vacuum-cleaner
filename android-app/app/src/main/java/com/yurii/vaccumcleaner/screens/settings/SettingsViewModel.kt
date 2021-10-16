package com.yurii.vaccumcleaner.screens.settings

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.MainViewModel
import com.yurii.vaccumcleaner.ServiceWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

enum class BluetoothStatus {
    DISCONNECTED, CONNECTING, CONNECTED
}

enum class ServiceConnectionStatus {
    VALIDATED, VALIDATING, BROKEN
}


class SettingsViewModel(private val bluetoothDevice: BluetoothDevice, private val mainViewModel: MainViewModel) : ViewModel() {
    private val service: ServiceWrapper by lazy { ServiceWrapper(mainViewModel.bluetoothService) }
    private val _bluetoothStatus: MutableStateFlow<BluetoothStatus> = MutableStateFlow(BluetoothStatus.CONNECTING)
    val bluetoothStatus = _bluetoothStatus.asStateFlow()

    private val _serviceConnectionStatus: MutableStateFlow<ServiceConnectionStatus> = MutableStateFlow(ServiceConnectionStatus.VALIDATING)
    val serviceConnectionStatus = _serviceConnectionStatus.asStateFlow()

    init {
        connectBluetooth()
    }

    fun connectBluetooth() {
        viewModelScope.launch {
            initBluetoothConnection()
            validateServiceConnection()
        }
    }

    private suspend fun initBluetoothConnection(): Unit = try {
        _bluetoothStatus.value = BluetoothStatus.CONNECTING
        mainViewModel.createBluetoothCommunication(bluetoothDevice)
        _bluetoothStatus.value = BluetoothStatus.CONNECTED
    } catch (exception: Exception) {
        Timber.e(exception)
        _bluetoothStatus.value = BluetoothStatus.DISCONNECTED
        _serviceConnectionStatus.value = ServiceConnectionStatus.BROKEN
    }

    private suspend fun validateServiceConnection(): Unit = try {
        _serviceConnectionStatus.value = ServiceConnectionStatus.VALIDATING
        service.performValidationRequest()
        _serviceConnectionStatus.value = ServiceConnectionStatus.VALIDATED
    } catch (error: Exception) {
        Timber.e(error)
        _serviceConnectionStatus.value = ServiceConnectionStatus.BROKEN
    }

    class Factory(private val bluetoothDevice: BluetoothDevice, private val mainViewModel: MainViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(bluetoothDevice, mainViewModel) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}
