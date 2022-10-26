package com.yurii.vaccumcleaner.screens.panel.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.yurii.vaccumcleaner.R

class ShutDownButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    init {
        inflate(context, R.layout.fragment_layout_shut_down, this)
        isClickable = true
        isFocusable = true
    }
}