package com.yurii.vaccumcleaner.devices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.vaccumcleaner.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothDevicesViewModel : ViewModel() {
    sealed class BluetoothState {
        object None : BluetoothState()
        object BluetoothIsDisabled : BluetoothState()
        object BluetoothIsUnsupported : BluetoothState()
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    val broadcastReceiverBluetoothDeviceFound = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
            _bluetoothDevices.update(BluetoothDeviceItem(name = device.name, macAddress = device.address, isPaired = false))
        }
    }

    private val _bluetoothDevices: MutableStateFlow<List<BluetoothDeviceItem>> = MutableStateFlow(emptyList())
    val bluetoothDevices: StateFlow<List<BluetoothDeviceItem>> = _bluetoothDevices

    private val _bluetoothState: MutableStateFlow<BluetoothState> = MutableStateFlow(BluetoothState.None)
    val bluetoothState: StateFlow<BluetoothState> = _bluetoothState

//    private val eventChannel = Channel<Event>(Channel.BUFFERED)
//    val eventFlow: Flow<Event> = eventChannel.receiveAsFlow()

    init {
        if (bluetoothAdapter == null)
            _bluetoothState.value = BluetoothState.BluetoothIsUnsupported
        else {
            if (bluetoothAdapter.isEnabled) {
                bluetoothAdapter.startDiscovery()
                loadPairedDevices()
            } else
                _bluetoothState.value = BluetoothState.BluetoothIsDisabled
        }
    }

    fun connectBluetoothDevice(bluetoothDeviceItem: BluetoothDeviceItem) {

    }

    private fun loadPairedDevices() {
        val devices = bluetoothAdapter!!.bondedDevices.map {
            BluetoothDeviceItem(name = it.name, macAddress = it.address, isPaired = true)
        }

        _bluetoothDevices.update(devices)
    }


    class Factory() : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BluetoothDevicesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BluetoothDevicesViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}