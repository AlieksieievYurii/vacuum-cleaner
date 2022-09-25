package com.yurii.vaccumcleaner.screens.loading

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.MyApplication
import com.yurii.vaccumcleaner.Preferences
import com.yurii.vaccumcleaner.robot.RobotConnection
import com.yurii.vaccumcleaner.robot.RobotSocketDiscovery
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

@SuppressLint("StaticFieldLeak")
class InitialFragmentViewModel(
    private val context: Context,
    private val robotSocketDiscovery: RobotSocketDiscovery
) : ViewModel() {
    sealed class Event {
        object NavigateToControlPanel : Event()
        object NavigateToBindRobot : Event()
    }

    sealed class State {
        object Scanning : State()
        data class NotFound(val wasIpSaved: Boolean) : State()
        data class Connected(val ip: String) : State()
    }

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Scanning)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val savedIp = Preferences.getRobotIpAddress(context)
            if (savedIp != null) {
                if (robotSocketDiscovery.tryConnect(savedIp)) {
                    delay(2000)
                    RobotConnection.makeConnection(savedIp, MyApplication.ROBOT_SOCKET_PORT)
                    _state.value = State.Connected(savedIp)
                    delay(3000)
                    _event.emit(Event.NavigateToControlPanel)
                } else
                    _state.value = State.NotFound(wasIpSaved = true)
            } else
                startDiscovering()
        }
    }

    private suspend fun startDiscovering() {
        val r = robotSocketDiscovery.discover(viewModelScope)
        if (r.size > 1) {
            //TODO Handle the case when two Robots are found
        } else {
            if (r.isEmpty())
                _state.value = State.NotFound(wasIpSaved = false)
            else {
                RobotConnection.makeConnection(r.first(), 1489)
                Preferences.saveRobotIpAddress(context, r.first())
                _state.value = State.Connected(r.first())
            }
        }
    }

    private fun navigateToControlPanel() {
        sendEvent(Event.NavigateToControlPanel)
    }

    fun navigateToBindRobot() {
        sendEvent(Event.NavigateToBindRobot)
    }

    fun rescan() {
        _state.value = State.Scanning
        viewModelScope.launch { startDiscovering() }
    }

    private fun sendEvent(event: Event) = viewModelScope.launch {
        _event.emit(event)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val context: Context,
        private val robotSocketDiscovery: RobotSocketDiscovery
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InitialFragmentViewModel::class.java))
                return InitialFragmentViewModel(context, robotSocketDiscovery) as T
            throw IllegalStateException("Given the model class is not assignable from SavedMusicViewModel class")
        }
    }
}