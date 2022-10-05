package com.yurii.vaccumcleaner.screens.algo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Algorithm
import com.yurii.vaccumcleaner.robot.AlgorithmScript
import com.yurii.vaccumcleaner.robot.ArgumentValue
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.IllegalStateException

class AlgorithmSetupViewModel(private val robot: Robot) : ViewModel() {
    sealed class Event {
        object CloseFragment : Event()
        data class ShowError(val exception: Throwable) : Event()
    }

    private val _algorithmNames = MutableStateFlow<List<String>?>(null)
    val algorithmNames = _algorithmNames.asStateFlow()

    private val _currentAlgorithm = MutableStateFlow<AlgorithmScript?>(null)
    val currentAlgorithm = _currentAlgorithm.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _algorithms = ArrayList<AlgorithmScript>()

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        _isLoading.value = false
        sendEvent(Event.ShowError(error))
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + errorHandler)

    init {
        netWorkScope.launch {
            delay(1000)
            val response = robot.getAlgorithms()
            _algorithms.apply {
                clear()
                addAll(response.algorithms)
            }

            _algorithmNames.value = response.algorithms.map { it.name }
            setAlgorithm(response.currentAlgorithmName)

            _isLoading.value = false
        }
    }

    fun setAlgorithm(name: String) {
        _currentAlgorithm.value = _algorithms.find { it.name == name }
    }

    fun applySettings(arguments: List<ArgumentValue>) {
        netWorkScope.launch {
            _isLoading.value = true
            robot.setAlgorithm(Algorithm(name = _currentAlgorithm.value!!.name, arguments = arguments))
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