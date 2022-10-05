package com.yurii.vaccumcleaner.screens.panel.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentLayoutHeaderWidgetBinding
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.ui.BatteryView
import kotlinx.coroutines.flow.StateFlow
import java.lang.IllegalStateException

class HeaderWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    sealed class BatteryState {
        object Charging : BatteryState()
        object Charged : BatteryState()
        data class Working(val capacity: Int, val voltage: Float) : BatteryState()
    }

    private val binding: FragmentLayoutHeaderWidgetBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.fragment_layout_header_widget, this, true
    )

    init {
        isClickable = true
        isFocusable = true
    }

    fun observeBatteryState(stateFlow: StateFlow<BatteryState>, lifecycleOwner: LifecycleOwner) {
        stateFlow.observeOnLifecycle(lifecycleOwner) {
            when (it) {
                BatteryState.Charged -> setBatteryIsCharged()
                BatteryState.Charging -> setBatteryIsCharging()
                is BatteryState.Working -> setBatteryIsWorking(it.capacity, it.voltage)
            }
        }
    }

    private fun setBatteryIsCharging() {
        binding.apply {
            battery.state = BatteryView.State.CHARGING
            batteryInfo.text = resources.getString(R.string.label_charging)
        }
    }

    private fun setBatteryIsCharged() {
        binding.apply {
            battery.state = BatteryView.State.CHARGED
            batteryInfo.text = resources.getString(R.string.label_charged)
        }
    }

    private fun setBatteryIsWorking(capacity: Int, voltage: Float) {
        binding.apply {
            battery.state = when (capacity * 6 / 100) {
                0 -> BatteryView.State.ZERO_CELLS
                1 -> BatteryView.State.ONE_CELL
                2 -> BatteryView.State.TWO_CELLS
                3 -> BatteryView.State.THREE_CELLS
                4 -> BatteryView.State.FOUR_CELLS
                5 -> BatteryView.State.FIVE_CELLS
                6 -> BatteryView.State.SIX_CELLS
                else -> throw IllegalStateException("Out of range of battery capacity")
            }

            batteryInfo.text = resources.getString(R.string.template_battery_info, capacity, voltage)
        }
    }

    fun setLidStatus(isLidOpen: Boolean) {
        binding.layoutLidOpened.isVisible = isLidOpen
    }

    fun setDustBoxStatus(isDustBoxOut: Boolean) {
        binding.layoutBoxOut.isVisible = isDustBoxOut
    }
}