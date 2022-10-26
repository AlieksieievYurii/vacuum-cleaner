package com.yurii.vaccumcleaner.utils.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.yurii.vaccumcleaner.R

fun showConfirmation(context: Context, titleResId: Int, messageResId: Int, onConfirmed: () -> Unit) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(titleResId)
    builder.setMessage(messageResId)
    builder.setPositiveButton(R.string.label_yes) { _, _ -> onConfirmed() }
    builder.setNegativeButton(R.string.label_cancel) { _, _ ->
        // User cancelled the dialog
    }
    builder.create().show()
}