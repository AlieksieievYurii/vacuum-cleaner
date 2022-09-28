package com.yurii.vaccumcleaner.utils.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class BatteryView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    enum class State {
        ZERO_CELLS, ONE_CELL, TWO_CELLS, THREE_CELLS, FOUR_CELLS, FIVE_CELLS, SIX_CELLS, CHARGING, CHARGED
    }

    var state: State = State.CHARGING
    set(value) {
        field = value
        invalidate()
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var path = Path()
    private val batteryCoverLineColor = Color.parseColor("#F44336")
    private val cellColor = Color.parseColor("#73F013")

    private var chargingProgress = 0

    private val chargingAnimation = ValueAnimator.ofInt(0, 7).apply {
        duration = 3000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            val value = it.animatedValue as Int
            chargingProgress = value
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBatteryCover(canvas)
        when (state) {
            State.CHARGING -> {
                if (!chargingAnimation.isStarted)
                    chargingAnimation.start()
                (0..chargingProgress).forEach {
                    drawCell(canvas, it)
                }
            }
            State.CHARGED -> (1..6).forEach { drawCell(canvas, it) }
            else -> (0..state.ordinal).forEach { drawCell(canvas, it) }
        }
    }

    private fun drawBatteryCover(canvas: Canvas) {
        paint.color = batteryCoverLineColor
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE

        path.reset()

        path.moveTo(width * 0.05f, height * 0.05f)
        path.lineTo(width * 0.77f, height * 0.05f)

        path.moveTo(width * 0.77f, height * 0.05f)
        path.lineTo(width * 0.77f, height * 0.3f)

        path.moveTo(width * 0.77f, height * 0.3f)
        path.lineTo(width * 0.9f, height * 0.3f)

        path.moveTo(width * 0.9f, height * 0.3f)
        path.lineTo(width * 0.9f, height * 0.7f)

        path.moveTo(width * 0.9f, height * 0.7f)
        path.lineTo(width * 0.77f, height * 0.7f)

        path.moveTo(width * 0.77f, height * 0.7f)
        path.lineTo(width * 0.77f, height * 0.95f)

        path.moveTo(width * 0.77f, height * 0.95f)
        path.lineTo(width * 0.05f, height * 0.95f)

        path.moveTo(width * 0.05f, height * 0.95f)
        path.lineTo(width * 0.05f, height * 0.05f)

        canvas.drawPath(path, paint)
    }

    private fun drawCell(canvas: Canvas, cellNumber: Int) {
        paint.style = Paint.Style.FILL
        paint.color = cellColor

        when (cellNumber) {
            1 -> canvas.drawRoundRect(width * 0.1f, height * 0.1f, width * 0.2f, height * 0.9f, 100f, 100f, paint)
            2 -> canvas.drawRoundRect(width * 0.23f, height * 0.1f, width * 0.33f, height * 0.9f, 100f, 100f, paint)
            3 -> canvas.drawRoundRect(width * 0.36f, height * 0.1f, width * 0.46f, height * 0.9f, 100f, 100f, paint)
            4 -> canvas.drawRoundRect(width * 0.49f, height * 0.1f, width * 0.59f, height * 0.9f, 100f, 100f, paint)
            5 -> canvas.drawRoundRect(width * 0.62f, height * 0.1f, width * 0.72f, height * 0.9f, 100f, 100f, paint)
            6 -> canvas.drawRoundRect(width * 0.75f, height * 0.35f, width * 0.86f, height * 0.65f, 100f, 100f, paint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        chargingAnimation.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        chargingAnimation.pause()
    }
}