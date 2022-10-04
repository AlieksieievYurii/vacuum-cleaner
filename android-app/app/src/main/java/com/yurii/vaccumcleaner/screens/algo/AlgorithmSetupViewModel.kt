package com.yurii.vaccumcleaner.screens.algo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Algorithm
import com.yurii.vaccumcleaner.robot.AlgorithmScript
import com.yurii.vaccumcleaner.robot.ArgumentValue
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

class AlgorithmSetupViewModel(private val robot: Robot) : ViewModel() {
    sealed class Event {
        object CloseFragment : Event()
    }

    private val _scriptsList = MutableStateFlow<List<String>?>(null)
    val scriptsList = _scriptsList.asStateFlow()

    private val _currentScript = MutableStateFlow<AlgorithmScript?>(null)
    val currentScript = _currentScript.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _algorithmScripts = ArrayList<AlgorithmScript>()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            val response = robot.getAlgorithmScripts()
            _algorithmScripts.apply {
                clear()
                addAll(response.scripts)
            }

            _scriptsList.value = response.scripts.map { it.name }
            setScript(response.currentScript)

            _isLoading.value = false
        }
    }

    fun setScript(name: String) {
        _currentScript.value = _algorithmScripts.find { it.name == name }
    }

    fun applySettings(parameters: List<ArgumentValue>) {
        viewModelScope.launch {
            _isLoading.value = true
            robot.setAlgorithmScript(Algorithm(name = _currentScript.value!!.name, parameters = parameters))
            delay(1000)
            _isLoading.value = false
            sendEvent(Event.CloseFragment)
        }
    }

    private fun sendEvent(event: Event) = viewModelScope.launch {
        _event.emit(event)
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(private val robot: Robot) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlgorithmSetupViewModel::class.java))
                return AlgorithmSetupViewModel(robot) as T
            throw IllegalStateException("Given the model class is not assignable from AlgorithmSetupViewModel class")
        }
    }
}