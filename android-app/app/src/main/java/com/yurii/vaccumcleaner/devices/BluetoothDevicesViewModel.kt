package com.yurii.vaccumcleaner.devices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import com.yurii.vaccumcleaner.update
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BluetoothDevicesViewModel : ViewModel() {
    sealed class BluetoothState {
        object None : BluetoothState()
        object BluetoothIsDisabled : BluetoothState()
        object BluetoothIsUnsupported : BluetoothState()
    }

    sealed class Event {
        object RequestToTurnOnBluetooth : Event()
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON)
                        _bluetoothState.value = BluetoothState.None
                    else
                        _bluetoothState.value = BluetoothState.BluetoothIsDisabled
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    _bluetoothDevices.update(BluetoothDeviceItem(name = device.name, macAddress = device.address, isPaired = false))
                }
            }
        }
    }

    private val _bluetoothDevices: MutableStateFlow<List<BluetoothDeviceItem>> = MutableStateFlow(emptyList())
    val bluetoothDevices: StateFlow<List<BluetoothDeviceItem>> = _bluetoothDevices

    private val _bluetoothState: MutableStateFlow<BluetoothState> = MutableStateFlow(BluetoothState.None)
    val bluetoothState: StateFlow<BluetoothState> = _bluetoothState

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = eventChannel.receiveAsFlow()

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

    fun askToTurnOnBluetooth() = viewModelScope.launch {
        eventChannel.send(Event.RequestToTurnOnBluetooth)
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