package com.yurii.vaccumcleaner.screens.pid

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.vaccumcleaner.robot.Robot
import com.yurii.vaccumcleaner.utils.Empty
import java.lang.IllegalStateException

class PidSettingsViewModel(private val robot: Robot) : ViewModel() {
    val proportional = ObservableField(String.Empty)
    val integral = ObservableField(String.Empty)
    val derivative = ObservableField(String.Empty)

    fun apply() {

    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val robot: Robot) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PidSettingsViewModel::class.java))
                return PidSettingsViewModel(robot) as T
            throw IllegalStateException("Given the model class is not assignable from PanelViewModel class")
        }
    }
}