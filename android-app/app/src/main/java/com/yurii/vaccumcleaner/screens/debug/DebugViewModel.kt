package com.yurii.vaccumcleaner.screens.debug

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.service.Service
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

enum class BluetoothStatus {
    DISCONNECTED, CONNECTING, CONNECTED
}

class DebugViewModel(bluetoothDevice: BluetoothDevice) : ViewModel() {
    private val service = Service(viewModelScope, bluetoothDevice, requestHandlers = emptyList())
    private val _bluetoothStatus: MutableStateFlow<BluetoothStatus> = MutableStateFlow(BluetoothStatus.DISCONNECTED)
    val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus

    private val coroutineException = CoroutineExceptionHandler { _, exception ->
        _bluetoothStatus.value = BluetoothStatus.DISCONNECTED
    }

    init {
        connectBluetooth()
    }

    fun connectBluetooth() {
        viewModelScope.launch(coroutineException) {
            _bluetoothStatus.value = BluetoothStatus.CONNECTING
            service.start()
            _bluetoothStatus.value = BluetoothStatus.CONNECTED
        }
    }

    class Factory(private val bluetoothDevice: BluetoothDevice) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DebugViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DebugViewModel(bluetoothDevice) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}