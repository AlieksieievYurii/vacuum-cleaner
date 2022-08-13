package com.yurii.vaccumcleaner.utils

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

@BindingAdapter("isVisible")
fun isVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}
@BindingAdapter("isEnabled")
fun isEnabled(view: View, isEnabled: Boolean) {
    view.isEnabled = isEnabled
}