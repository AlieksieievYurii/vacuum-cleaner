package com.yurii.vaccumcleaner.screens.algo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.vaccumcleaner.robot.Robot
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.IllegalStateException

class AlgorithmSetupViewModel(private val robot: Robot) : ViewModel() {

    fun test() {
        viewModelScope.launch {
            Timber.i(robot.getAlgorithmScripts().toString())
        }
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