package com.yurii.vaccumcleaner.utils.ui

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.yurii.vaccumcleaner.R

fun showError(rootView: View, title: String, error: Throwable) {
    val snackBarCustomLayout = LayoutInflater.from(rootView.context).inflate(R.layout.view_snackbar, null)
    val snackBar = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE)
    snackBar.view.setBackgroundColor(Color.TRANSPARENT)
    val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout
    snackBarLayout.setPadding(0, 0, 0, 0)
    snackBarLayout.addView(snackBarCustomLayout)
    snackBarCustomLayout.findViewById<TextView>(R.id.title).text = title
    snackBarCustomLayout.findViewById<Button>(R.id.open).setOnClickListener {
        AlertDialog.Builder(rootView.context).setTitle(title).setMessage(error.message ?: "No details").setPositiveButton("Got it") { _, _ ->
            //Nothing
        }.setIcon(R.drawable.ic_red_error_outline_24).show()
        snackBar.dismiss()
    }

    snackBar.show()
}