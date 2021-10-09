package com.yurii.vaccumcleaner.screens.wifi

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.service.Service
import kotlinx.coroutines.launch



class WifiSettingsViewModel(bluetoothDevice: BluetoothDevice) : ViewModel() {
    private val service = Service(viewModelScope, bluetoothDevice, listOf())

    init {
        viewModelScope.launch {
            service.start()
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