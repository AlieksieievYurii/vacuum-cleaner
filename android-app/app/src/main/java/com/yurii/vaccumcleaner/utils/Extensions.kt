package com.yurii.vaccumcleaner.utils

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

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

fun Int.reverseBytes(): Int {
    val v0 = ((this ushr 0) and 0xFF)
    val v1 = ((this ushr 8) and 0xFF)
    val v2 = ((this ushr 16) and 0xFF)
    val v3 = ((this ushr 24) and 0xFF)
    return (v0 shl 24) or (v1 shl 16) or (v2 shl 8) or (v3 shl 0)
}

fun <E> MutableList<E>.pop(lock: Any, predicate: (E) -> Boolean): E? {
    synchronized(lock) {
        val res = this.find(predicate)
        return res?.also { remove(it) }
    }
}

fun <E> MutableList<E>.synchronizedAppend(lock: Any, element: E) {
    synchronized(lock) { this.add(element) }
}

fun View.setPressedUnpressedListener(onPress: () -> Unit, onRelease: () -> Unit) {
    this.setOnTouchListener { v, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                onPress()
                false
            }
            MotionEvent.ACTION_UP -> {
                onRelease()
                v.performClick()
                false
            }
            MotionEvent.ACTION_CANCEL -> {
                onRelease()
                false
            }
            else -> false
        }
    }
}

fun SeekBar.setProgressListener(onProgressChanged: (progress: Int) -> Unit, onProgress: (progress: Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            onProgressChanged(p1)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
            //Not used
        }

        override fun onStopTrackingTouch(p0: SeekBar) {
            onProgress(p0.progress)
        }
    })
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun <T : Parcelable> Intent.requireParcelableExtra(name: String): T {
    return this.getParcelableExtra(name) ?: throw IllegalArgumentException("Parcelable extra '$name' is required")
}

fun LottieAnimationView.runAnimation(resource: Int, infinitive: Boolean = false) {
    repeatCount = if (infinitive) ValueAnimator.INFINITE else 0
    setAnimation(resource)
    playAnimation()
}