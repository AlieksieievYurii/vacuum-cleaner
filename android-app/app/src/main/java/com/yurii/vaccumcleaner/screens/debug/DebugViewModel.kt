package com.yurii.vaccumcleaner.screens.debug

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DebugViewModel(private val bluetoothDevice: BluetoothDevice) : ViewModel() {

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