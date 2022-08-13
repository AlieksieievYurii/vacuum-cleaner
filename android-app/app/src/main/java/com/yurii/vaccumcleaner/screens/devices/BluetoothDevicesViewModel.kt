package com.yurii.vaccumcleaner.screens.devices

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.yurii.vaccumcleaner.Application
import com.yurii.vaccumcleaner.utils.addUnique
import com.yurii.vaccumcleaner.utils.replace
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BluetoothDevicesViewModel(application: Application) : AndroidViewModel(application) {
    sealed class BluetoothState {
        data class Ready(val isDiscovering: Boolean) : BluetoothState()
        object BluetoothIsDisabled : BluetoothState()
        object PermissionsDenied : BluetoothState()
        object BluetoothIsUnsupported : BluetoothState()
    }

    sealed class Event {
        object ShowMessageUnableToPair : Event()
        data class NavigateToSettingsFragment(val bluetoothDevice: BluetoothDevice) : Event()
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
                        loadPairedDevicesAndStartDiscovering()
                        _bluetoothState.value = BluetoothState.Ready(isDiscovering = true)
                    } else
                        _bluetoothState.value = BluetoothState.BluetoothIsDisabled
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    _bluetoothDevices.addUnique(BluetoothDeviceItem(bluetoothDevice = device, isPaired = false))
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING)
                        onPaired(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!)
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    if (_bluetoothState.value is BluetoothState.Ready)
                        _bluetoothState.value = BluetoothState.Ready(isDiscovering = true)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (_bluetoothState.value is BluetoothState.Ready)
                        _bluetoothState.value = BluetoothState.Ready(isDiscovering = false)
                }
            }
        }
    }

    private val _bluetoothDevices: MutableStateFlow<List<BluetoothDeviceItem>> = MutableStateFlow(emptyList())
    val bluetoothDevices: StateFlow<List<BluetoothDeviceItem>> = _bluetoothDevices

    private val _bluetoothState: MutableStateFlow<BluetoothState> = MutableStateFlow(BluetoothState.Ready(isDiscovering = false))
    val bluetoothState: StateFlow<BluetoothState> = _bluetoothState

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = eventChannel.receiveAsFlow()

    init {
        if (ContextCompat.checkSelfPermission(application, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            _bluetoothState.value = BluetoothState.PermissionsDenied
        else
            startBluetooth()
    }

    private fun startBluetooth() {
        if (bluetoothAdapter == null)
            _bluetoothState.value = BluetoothState.BluetoothIsUnsupported
        else {
            if (bluetoothAdapter.isEnabled)
                loadPairedDevicesAndStartDiscovering()
            else
                _bluetoothState.value = BluetoothState.BluetoothIsDisabled
        }
    }

    fun loadPairedDevicesAndStartDiscovering() {
        _bluetoothDevices.value = emptyList()
        loadPairedDevices()
        bluetoothAdapter!!.startDiscovery()
    }

    fun permissionsAreGranted() {
        _bluetoothState.value = BluetoothState.Ready(isDiscovering = true)
        startBluetooth()
    }

    fun connectBluetoothDevice(bluetoothDeviceItem: BluetoothDeviceItem) {
        if (bluetoothDeviceItem.isPaired)
            navigateToSettings(bluetoothDeviceItem)
        else
            pairBluetoothDevice(bluetoothDeviceItem)
    }

    private fun navigateToSettings(bluetoothDeviceItem: BluetoothDeviceItem) {
        sendEvent(Event.NavigateToSettingsFragment(bluetoothDeviceItem.bluetoothDevice))
    }

    private fun pairBluetoothDevice(bluetoothDeviceItem: BluetoothDeviceItem) {
        if (bluetoothDeviceItem.bluetoothDevice.createBond())
            _bluetoothDevices.replace(bluetoothDeviceItem, bluetoothDeviceItem.copy(isPairing = true))
        else
            sendEvent(Event.ShowMessageUnableToPair)
    }


    private fun loadPairedDevices() {
        val devices = bluetoothAdapter!!.bondedDevices.map {
            BluetoothDeviceItem(bluetoothDevice = it, isPaired = true)
        }

        _bluetoothDevices.addUnique(devices)
    }

    private fun sendEvent(event: Event) = viewModelScope.launch {
        eventChannel.send(event)
    }

    private fun onPaired(device: BluetoothDevice) {

    }

    companion object {
        val REQUIRED_BROADCAST_FILTERS = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BluetoothDevicesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BluetoothDevicesViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}