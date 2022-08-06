package com.yurii.vaccumcleaner.screens.binder


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalStateException

class BinderViewModel : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BinderViewModel::class.java))
                return BinderViewModel() as T
            throw IllegalStateException("Given the model class is not assignable from SavedMusicViewModel class")
        }
    }
}