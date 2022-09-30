package com.yurii.vaccumcleaner.utils.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import kotlinx.coroutines.flow.StateFlow

class LoadingDialog(context: Context) {
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create().apply { window!!.setBackgroundDrawableResource(R.color.transparent) }
    }

    fun show() {
        if (!dialog.isShowing)
            dialog.show()
    }

    fun close() {
        if (dialog.isShowing)
            dialog.dismiss()
    }

    fun observeState(state: StateFlow<Boolean>, lifecycleOwner: LifecycleOwner, onChange: ((isLoading: Boolean) -> Unit)? = null) {
        state.observeOnLifecycle(lifecycleOwner) { isLoading ->
            onChange?.invoke(isLoading)
            if (isLoading)
                show()
            else
                close()
        }
    }
}