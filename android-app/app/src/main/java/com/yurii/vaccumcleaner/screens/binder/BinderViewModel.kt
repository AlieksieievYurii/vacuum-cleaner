package com.yurii.vaccumcleaner.screens.binder


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.vaccumcleaner.screens.loading.InitialFragmentViewModel
import com.yurii.vaccumcleaner.utils.requireParcelableExtra
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.lang.IllegalStateException

class BinderViewModel(private val context: Context) : ViewModel() {
    sealed class State {
        object BluetoothIsDisabled : State()
        object PermissionDenied : State()
        data class Discovering(val found: Int = 0) : State()
        object RobotFound : State()
        object RobotNotFound : State()
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val _event = MutableSharedFlow<InitialFragmentViewModel.Event>()
    val event = _event.asSharedFlow()

    private val _currentState = MutableStateFlow<State>(State.Discovering())
    val currentState = _currentState.asStateFlow()

    private var foundBluetoothDevices: Int = 0

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> onBluetoothStateHasChanged(intent)
                BluetoothDevice.ACTION_FOUND -> onBluetoothDeviceFound(intent)
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        Timber.i("Is paired: ${intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!}")
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    foundBluetoothDevices = 0
                    _currentState.value = State.Discovering()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (_currentState.value !is State.RobotFound)
                        _currentState.value = State.RobotNotFound
                }
            }
        }
    }

    init {
        validateAndStart()
    }

    private fun onBluetoothStateHasChanged(intent: Intent) {
        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
            Timber.i("Bluetooth: Start discovering...")
            startBluetooth()
        } else {
            Timber.i("Bluetooth: Disabled!")
            _currentState.value = State.BluetoothIsDisabled
        }
    }

    private fun onBluetoothDeviceFound(intent: Intent) {
        if (_currentState.value is State.RobotFound)
            return

        foundBluetoothDevices++
        val device: BluetoothDevice = intent.requireParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

        //I do not know how to identify the robot so decided to do that by MAC -> I know this is bullshit
        if (device.address == ROBOT_MAC_ADDRESS) {
            Timber.i("Found: $device. Name: ${device.name} ${device.uuids}")
            _currentState.value = State.RobotFound
            bluetoothAdapter!!.cancelDiscovery()
        } else
            _currentState.value = State.Discovering(foundBluetoothDevices)
    }

    private fun validateAndStart() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            _currentState.value = State.PermissionDenied
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            _currentState.value = State.BluetoothIsDisabled
            return
        }

        startBluetooth()
    }

    fun rescan() {
        startBluetooth()
    }

    fun bluetoothPermissionsAreGranted() {
        bluetoothAdapter!!.startDiscovery()
    }

    private fun startBluetooth() {
        if (bluetoothAdapter == null)
            Timber.i("Bluetooth is not supported")
        else {
            if (bluetoothAdapter.isEnabled) {
                Timber.i("Bluetooth is enabled!")
                bluetoothAdapter.startDiscovery()
            } else
                Timber.i("Bluetooth is disabled")
        }
    }

    companion object {
        const val ROBOT_MAC_ADDRESS = "B8:27:EB:B8:34:3E"
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BinderViewModel::class.java))
                return BinderViewModel(context) as T
            throw IllegalStateException("Given the model class is not assignable from SavedMusicViewModel class")
        }
    }
}