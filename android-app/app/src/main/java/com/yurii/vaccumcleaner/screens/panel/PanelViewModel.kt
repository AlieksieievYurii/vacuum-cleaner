package com.yurii.vaccumcleaner.screens.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

import java.lang.IllegalStateException

class PanelViewModel(private val robot: Robot) : ViewModel() {
    sealed class Event {
        object NavigateToControlFragment : Event()
    }

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    fun openManualControlFragment() {
        viewModelScope.launch {
            // _event.emit(Event.NavigateToControlFragment)
        }
    }

    fun openCleaningHistory() {

    }

    fun openPidSettings() {

    }

    fun openWifiSettings() {

    }

    fun openCleaningAlgoSettings() {

    }

    fun startCleaning() {

    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val robot: Robot) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PanelViewModel::class.java))
                return PanelViewModel(robot) as T
            throw IllegalStateException("Given the model class is not assignable from PanelViewModel class")
        }
    }
}