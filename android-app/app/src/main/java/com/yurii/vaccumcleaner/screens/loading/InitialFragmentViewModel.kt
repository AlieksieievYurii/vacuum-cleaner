package com.yurii.vaccumcleaner.screens.loading

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.MyApplication
import com.yurii.vaccumcleaner.Preferences
import com.yurii.vaccumcleaner.robot.CleaningStatusEnum
import com.yurii.vaccumcleaner.robot.Robot
import com.yurii.vaccumcleaner.robot.RobotConnection
import com.yurii.vaccumcleaner.robot.RobotSocketDiscovery
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.IllegalStateException
import android.net.wifi.WifiManager
import com.yurii.vaccumcleaner.utils.ServiceContextManager
import kotlinx.coroutines.*


class InitialFragmentViewModel(
    serviceContextManager: ServiceContextManager,
    private val preferences: Preferences,
    private val robotSocketDiscovery: RobotSocketDiscovery
) : ViewModel() {
    sealed class Event {
        object NavigateToControlPanel : Event()
        object NavigateToBindRobot : Event()
        object NavigateToExecutionScreen : Event()
    }

    sealed class State {
        object WifiDisabled : State()
        object Scanning : State()
        data class Error(val errorMessage: String) : State()
        data class NotFound(val wasIpSaved: Boolean) : State()
        data class Connected(val ip: String) : State()
    }

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiManager.WIFI_STATE_ENABLED) {
                    viewModelScope.launch {
                        _state.value = State.Scanning
                        delay(5000)
                        tryToConnect()
                    }
                } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                    _state.value = State.WifiDisabled
                }
            }
        }
    }

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Scanning)
    val state = _state.asStateFlow()

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        _state.value = State.Error(error.message ?: "No error message")
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + errorHandler)

    init {
        if (serviceContextManager.isWifiEnabled()) {
            tryToConnect()
        } else
            _state.value = State.WifiDisabled
    }

    private fun tryToConnect() = netWorkScope.launch {
        val savedIp = preferences.getRobotIpAddress()
        if (savedIp != null) {
            if (robotSocketDiscovery.tryConnect(savedIp)) {
                delay(1000)
                RobotConnection.makeConnection(savedIp, MyApplication.ROBOT_SOCKET_PORT)
                setConnectedStatus(savedIp)
            } else
                startDiscovering()
        } else
            startDiscovering()
    }

    private suspend fun startDiscovering() {
        val r = robotSocketDiscovery.discover(netWorkScope)
        if (r.size > 1) {
            _state.value = State.Error("Found more than one robot")
        } else {
            if (r.isEmpty())
                _state.value = State.NotFound(wasIpSaved = false)
            else {
                val robotIp = r.first()
                RobotConnection.makeConnection(robotIp, 1489)
                preferences.saveRobotIpAddress(robotIp)
                setConnectedStatus(robotIp)
            }
        }
    }

    private suspend fun setConnectedStatus(robotIp: String) {
        val robot: Robot = RobotConnection.getRobotAPI()
        val status = robot.getCleaningStatus().status
        _state.value = State.Connected(robotIp)
        delay(2000)
        if (status == CleaningStatusEnum.NONE)
            sendEvent(Event.NavigateToControlPanel)
        else
            sendEvent(Event.NavigateToExecutionScreen)
    }

    fun navigateToBindRobot() {
        sendEvent(Event.NavigateToBindRobot)
    }

    fun rescan() {
        _state.value = State.Scanning
        netWorkScope.launch { startDiscovering() }
    }

    private fun sendEvent(event: Event) = viewModelScope.launch {
        _event.emit(event)
    }

    companion object {
        val REQUIRED_BROADCAST_FILTERS = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val serviceContextManager: ServiceContextManager,
        private val preferences: Preferences,
        private val robotSocketDiscovery: RobotSocketDiscovery
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InitialFragmentViewModel::class.java))
                return InitialFragmentViewModel(serviceContextManager, preferences, robotSocketDiscovery) as T
            throw IllegalStateException("Given the model class is not assignable from SavedMusicViewModel class")
        }
    }
}