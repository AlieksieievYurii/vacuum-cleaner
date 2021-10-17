package com.yurii.vaccumcleaner

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

class FlowObserver<T>(
    private val lifecycleOwner: LifecycleOwner,
    private val flow: Flow<T>,
    private val collector: suspend (T) -> Unit
) : LifecycleObserver {
    private var job: Job? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        job = lifecycleOwner.lifecycleScope.launch {
            flow.collectLatest { collector(it) }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        job?.cancel()
        job = null
    }
}

inline fun <reified T> Flow<T>.observeOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    noinline collector: suspend (T) -> Unit
) = FlowObserver(lifecycleOwner, this, collector)

fun <T> MutableStateFlow<List<T>>.addUnique(newList: List<T>) {
    this.value = this.value + newList
}

fun <T> MutableStateFlow<List<T>>.addUnique(item: T) {
    if (!this.value.contains(item))
        this.value = this.value + item
}

fun <T> MutableStateFlow<List<T>>.replace(item: T, newItem: T) {
    this.value = this.value.map { if (it == item) newItem else it }
}

val String.Companion.Empty
    get() = ""

val <T> ObservableField<T>.value: T
    get() = this.get()!!
