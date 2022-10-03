package com.yurii.vaccumcleaner.screens.algo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.AlgorithmScript
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

class AlgorithmSetupViewModel(private val robot: Robot) : ViewModel() {

    private val _scriptsList = MutableStateFlow<List<String>?>(null)
    val scriptsList = _scriptsList.asStateFlow()

    private val _currentScript = MutableStateFlow<AlgorithmScript?>(null)
    val currentScript = _currentScript.asStateFlow()

    private val _algorithmScripts = ArrayList<AlgorithmScript>()

    init {
        viewModelScope.launch {
            val response = robot.getAlgorithmScripts()
            _algorithmScripts.apply {
                clear()
                addAll(response.scripts)
            }

            _scriptsList.value = response.scripts.map { it.name }
            setScript(response.currentScript)
        }
    }

    fun setScript(name: String) {
        _currentScript.value = _algorithmScripts.find { it.name == name }
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