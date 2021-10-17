package com.yurii.vaccumcleaner

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.service.Service
import java.lang.IllegalStateException

class MainViewModel : ViewModel() {

    private var _bluetoothService: Service? = null

    val bluetoothService: Service
        get() {
            return _bluetoothService ?: throw IllegalStateException("Service is not created!")
        }

    suspend fun createBluetoothCommunication(bluetoothDevice: BluetoothDevice) {
        _bluetoothService = Service(viewModelScope, bluetoothDevice, requestHandlers = listOf()).also {
            it.start()
        }
    }


    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}