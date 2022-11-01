package com.yurii.vaccumcleaner.screens.control

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.utils.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentControlBinding
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.setPressedUnpressedListener
import com.yurii.vaccumcleaner.utils.setProgressListener
import com.yurii.vaccumcleaner.utils.ui.showError

class ManualControlFragment : Fragment(R.layout.fragment_control) {
    private val binding: FragmentControlBinding by viewBinding()
    private val viewModel: ManualControlViewModel by viewModels { Injector.provideManualControlViewModel() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeRuntimeRobotInputData()
        binding.apply {
            btnForward.setPressedUnpressedListener(onPress = viewModel::moveForward, onRelease = viewModel::stop)
            btnBackward.setPressedUnpressedListener(onPress = viewModel::moveBackward, onRelease = viewModel::stop)
            btnLeft.setPressedUnpressedListener(onPress = viewModel::turnLeft, onRelease = viewModel::stop)
            btnRight.setPressedUnpressedListener(onPress = viewModel::turnRight, onRelease = viewModel::stop)
        }


        binding.apply {
            initSwitchSeekView(
                vacuumMotorSwitch, vacuumMotorSpeed, vacuumMotorTargetSpeed, 50, R.string.template_number_with_percentage
            ) { isChecked: Boolean, value: Int -> viewModel.setVacuumMotorSpeed(if (isChecked) value else 0) }

            initSwitchSeekView(
                mainBrushMotorSwitch, mainBrushMotorSpeed, mainBrushMotorTargetSpeed, 50, R.string.template_number_with_percentage
            ) { isChecked: Boolean, value: Int -> viewModel.setMainBrushMotorSpeed(if (isChecked) value else 0) }

            initSwitchSeekView(
                rightBrushMotorSwitch, rightBrushMotorSpeed, rightBrushMotorTargetSpeed, 50, R.string.template_number_with_percentage
            ) { isChecked: Boolean, value: Int -> viewModel.setRightBrushSpeed(if (isChecked) value else 0) }

            initSwitchSeekView(
                leftBrushMotorSwitch, leftBrushMotorSpeed, leftBrushMotorTargetSpeed, 50, R.string.template_number_with_percentage
            ) { isChecked: Boolean, value: Int -> viewModel.setLeftBrushSpeed(if (isChecked) value else 0) }

            initSwitchSeekView(
                withBreak, wheelSpeed, targetSpeed, viewModel.wheelSpeed, R.string.template_speed_cm_per_minute
            ) { isChecked: Boolean, value: Int ->
                viewModel.wheelSpeed = value
                viewModel.withBreak = isChecked
            }
        }

        binding.headerWidget.observeBatteryState(viewModel.batteryState, viewLifecycleOwner)

        viewModel.event.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                is ManualControlViewModel.Event.ShowError -> showError(binding.root, getString(R.string.label_error_occurred), event.exception)
            }

        }
    }

    private fun initSwitchSeekView(
        switch: SwitchCompat,
        seekBar: SeekBar,
        textView: TextView,
        defaultValue: Int,
        pattern: Int,
        onChange: (isChecked: Boolean, value: Int) -> Unit
    ) {
        seekBar.progress = defaultValue
        textView.text = getString(pattern, defaultValue)
        seekBar.setProgressListener(onProgressChanged = { value ->
            textView.text = getString(pattern, value)
        }, onProgress = { onChange(switch.isChecked, it) })

        switch.setOnCheckedChangeListener { _, isChecked -> onChange(isChecked, seekBar.progress) }
    }

    private fun observeRuntimeRobotInputData() {
        viewModel.runTimeRobotData.observeOnLifecycle(viewLifecycleOwner) { data ->
            data?.run {
                binding.infRangeRadarValues.text = getString(
                    R.string.label_range_sensors,
                    data.leftDistanceRange,
                    data.centerDistanceRange,
                    data.rightDistanceRange
                )
                binding.infBumper.text = getString(
                    R.string.label_bumper_hit, when {
                        data.leftBumperHit && data.rightBumperHit -> "Both"
                        data.leftBumperHit -> "Left"
                        data.rightBumperHit -> "Right"
                        else -> "None"
                    }
                )
                binding.headerWidget.setLidStatus(!data.isLidClosed)
                binding.headerWidget.setDustBoxStatus(!data.isDustBoxInserted)
                binding.infWheelsSpeed.text = getString(R.string.label_wheels_speed, data.leftWheelSpeed, data.rightWheelSpeed)
            }
        }
    }
}