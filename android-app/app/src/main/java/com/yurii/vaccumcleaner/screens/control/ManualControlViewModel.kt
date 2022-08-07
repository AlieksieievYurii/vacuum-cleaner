package com.yurii.vaccumcleaner.screens.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ManualControlViewModel : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManualControlViewModel::class.java))
                return ManualControlViewModel() as T
            throw IllegalStateException("Given the model class is not assignable from ManualControlViewModel class")
        }
    }
}