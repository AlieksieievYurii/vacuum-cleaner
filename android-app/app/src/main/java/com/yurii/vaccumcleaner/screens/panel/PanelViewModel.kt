package com.yurii.vaccumcleaner.screens.panel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.requesthandler.Communicator
import com.yurii.vaccumcleaner.requesthandler.RequestHandler
import com.yurii.vaccumcleaner.robot.WifiCommunicator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

import java.lang.IllegalStateException

class PanelViewModel(private val robotCommunicator: Communicator) : ViewModel() {
    sealed class Event {
        object NavigateToControlFragment : Event()
    }

    private val robotApi = RequestHandler(robotCommunicator, viewModelScope)

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    fun openManualControlFragment() {
        viewModelScope.launch {
            _event.emit(Event.NavigateToControlFragment)
        }
    }

    init {
        robotApi.start()
        viewModelScope.launch {
            val r = robotApi.send("/hello-world", TestRequest("Yurii", 22), TestResponse::class.java)
            Log.i("TEST", r.toString())
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val robotCommunicator: Communicator) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PanelViewModel::class.java))
                return PanelViewModel(robotCommunicator) as T
            throw IllegalStateException("Given the model class is not assignable from PanelViewModel class")
        }
    }
}