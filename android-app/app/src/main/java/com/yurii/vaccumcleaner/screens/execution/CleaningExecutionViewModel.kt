package com.yurii.vaccumcleaner.screens.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.vaccumcleaner.robot.Robot
import java.lang.IllegalStateException

class CleaningExecutionViewModel : ViewModel() {


    @Suppress("UNCHECKED_CAST")
    class Factory(private val robot: Robot) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CleaningExecutionViewModel::class.java))
                return CleaningExecutionViewModel() as T
            throw IllegalStateException("Given the model class is not assignable from CleaningExecutionViewModel class")
        }
    }
}