package com.yurii.vaccumcleaner.screens.binder


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.RobotBluetoothConnection
import com.yurii.vaccumcleaner.utils.requireParcelableExtra
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.IllegalStateException

class BinderViewModel(private val context: Context) : ViewModel() {
    sealed class State {
        object Initial : State()
        object BluetoothIsDisabled : State()
        object PermissionDenied : State()
        data class Discovering(val found: Int = 0) : State()
        object RobotPaired : State()
        object RobotNeedsToBePaired : State()
        object RobotIsPairing : State()
        object RobotPairingFailed : State()
        object RobotNotFound : State()
    }

    sealed class Event {
        object NavigateToWifiSettingsScreen : Event()
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    private val _currentState = MutableStateFlow<State>(State.Initial)
    val currentState = _currentState.asStateFlow()

    private var scannedBluetoothDevicesCount: Int = 0
    private var isPaired = false
    private var foundedRobot: BluetoothDevice? = null

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> onBluetoothStateHasChanged(intent)
                BluetoothDevice.ACTION_FOUND -> onBluetoothDeviceFound(intent)
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> onBoundStateChanged(intent)
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> _currentState.value = State.Discovering()
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (foundedRobot == null)
                        _currentState.value = State.RobotNotFound
                }
            }
        }
    }

    fun startDiscoveringRobot() {
        if (bluetoothAllowedAndEnabled())
            startBluetooth()
    }

    fun askToPair() {
        _currentState.value = State.RobotIsPairing
        foundedRobot!!.createBond()
    }

    fun rescan() {
        startBluetooth()
    }

    fun bluetoothPermissionsAreGranted() {
        startBluetooth()
    }

    fun retryPairing() {
        askToPair()
    }

    private fun onBoundStateChanged(intent: Intent) {
        val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
        val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

        if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING && !isPaired) {
            isPaired = true
            val robotBluetoothDevice: BluetoothDevice = intent.requireParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            makeBluetoothSocketConnectionAndNavigateToWifiSettingsScreen(robotBluetoothDevice)
        } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDING) {
            _currentState.value = State.RobotPairingFailed
        }
    }

    private fun onBluetoothStateHasChanged(intent: Intent) {
        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
            startDiscoveringRobot()
        } else {
            _currentState.value = State.BluetoothIsDisabled
        }
    }

    private fun onBluetoothDeviceFound(intent: Intent) {
        if (foundedRobot != null)
            return

        scannedBluetoothDevicesCount++
        val device: BluetoothDevice = intent.requireParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

        //I do not know how to identify the robot so decided to do that by MAC -> I know this is bullshit
        if (device.address == ROBOT_MAC_ADDRESS) {
            onRobotFound(device)
            bluetoothAdapter!!.cancelDiscovery()
        } else
            _currentState.value = State.Discovering(scannedBluetoothDevicesCount)
    }

    private fun onRobotFound(device: BluetoothDevice) {
        foundedRobot = device
        if (isPaired(device)) {
            _currentState.value = State.RobotPaired
            makeBluetoothSocketConnectionAndNavigateToWifiSettingsScreen(device)
        } else {
            _currentState.value = State.RobotNeedsToBePaired
        }
    }

    private fun bluetoothAllowedAndEnabled(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            _currentState.value = State.PermissionDenied
            return false
        }

        if (!bluetoothAdapter!!.isEnabled) {
            _currentState.value = State.BluetoothIsDisabled
            return false
        }

        return true
    }


    private fun makeBluetoothSocketConnectionAndNavigateToWifiSettingsScreen(device: BluetoothDevice) {
        viewModelScope.launch {
            RobotBluetoothConnection.makeConnection(device)
            _event.emit(Event.NavigateToWifiSettingsScreen)
        }
    }

    private fun startBluetooth() {
        scannedBluetoothDevicesCount = 0
        if (bluetoothAdapter == null)
            Timber.i("Bluetooth is not supported")
        else {
            if (bluetoothAdapter.isEnabled) {
                _currentState.value = State.Discovering()
                viewModelScope.launch {
                    delay(5000)
                    bluetoothAdapter.startDiscovery()
                }
            } else
                _currentState.value = State.BluetoothIsDisabled
        }
    }

    private fun isPaired(device: BluetoothDevice): Boolean {
        bluetoothAdapter!!.bondedDevices.forEach {
            if (it.address == device.address)
                return true
        }
        return false
    }

    companion object {
        const val ROBOT_MAC_ADDRESS = "B8:27:EB:B8:34:3E"

        val REQUIRED_BROADCAST_FILTERS = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
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