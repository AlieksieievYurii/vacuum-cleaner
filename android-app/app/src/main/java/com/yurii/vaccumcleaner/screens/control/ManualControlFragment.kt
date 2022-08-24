package com.yurii.vaccumcleaner.screens.control

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentControlBinding
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.setPressedUnpressedListener
import com.yurii.vaccumcleaner.utils.setProgressListener

class ManualControlFragment : Fragment(R.layout.fragment_control) {
    private val binding: FragmentControlBinding by viewBinding()
    private val viewModel: ManualControlViewModel by viewModels { Injector.provideManualControlViewModel() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeRuntimeRobotInputData()
        binding.btnForward.setPressedUnpressedListener(onPress = viewModel::moveForward, onRelease = viewModel::stop)
        binding.btnBackward.setPressedUnpressedListener(onPress = viewModel::moveBackward, onRelease = viewModel::stop)
        binding.btnLeft.setPressedUnpressedListener(onPress = viewModel::turnLeft, onRelease = viewModel::stop)
        binding.btnRight.setPressedUnpressedListener(onPress = viewModel::turnRight, onRelease = viewModel::stop)

        binding.apply {
            wheelSpeed.setProgressListener(onProgressChanged = { speedCmPerMinute ->
                targetSpeed.text = getString(R.string.template_speed_cm_per_minute, speedCmPerMinute)
            }, onProgress = { viewModel.wheelSpeed = it })

            vacuumMotorSpeed.setProgressListener(onProgressChanged = { speedInPercentage ->
                vacuumMotorTargetSpeed.text = getString(R.string.template_number_with_percentage, speedInPercentage)
            }, onProgress = { speedInPercentage -> if (vacuumMotorSwitch.isChecked) viewModel.setVacuumMotorSpeed(speedInPercentage) })

            mainBrushMotorSpeed.setProgressListener(onProgressChanged = { speedInPercentage ->
                mainBrushMotorTargetSpeed.text = getString(R.string.template_number_with_percentage, speedInPercentage)
            }, onProgress = { speedInPercentage -> if (mainBrushMotorSwitch.isChecked) viewModel.setMainBrushMotorSpeed(speedInPercentage) })

            rightBrushMotorSpeed.setProgressListener(onProgressChanged = { speedInPercentage ->
                rightBrushMotorTargetSpeed.text = getString(R.string.template_number_with_percentage, speedInPercentage)
            }, onProgress = { speedInPercentage -> if (rightBrushMotorSwitch.isChecked) viewModel.setRightBrushSpeed(speedInPercentage) })

            leftBrushMotorSpeed.setProgressListener(onProgressChanged = { speedInPercentage ->
                leftBrushMotorTargetSpeed.text = getString(R.string.template_number_with_percentage, speedInPercentage)
            }, onProgress = { speedInPercentage -> if (leftBrushMotorSwitch.isChecked) viewModel.setLeftBrushSpeed(speedInPercentage) })

            vacuumMotorSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setVacuumMotorSpeed(if (isChecked) vacuumMotorSpeed.progress else 0)
            }

            mainBrushMotorSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setMainBrushMotorSpeed(if (isChecked) mainBrushMotorSpeed.progress else 0)
            }

            rightBrushMotorSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setRightBrushSpeed(if (isChecked) rightBrushMotorSpeed.progress else 0)
            }

            leftBrushMotorSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setLeftBrushSpeed(if (isChecked) leftBrushMotorSpeed.progress else 0)
            }

            withBreak.setOnCheckedChangeListener { _, isChecked -> viewModel.withBreak = isChecked }
        }
    }

    private fun observeRuntimeRobotInputData() {
        viewModel.runTimeRobotData.observeOnLifecycle(viewLifecycleOwner) { data ->
            data?.run {
                binding.cvDustBoxPulled.isVisible = !data.isDustBoxInserted
                binding.cvLidWarning.isVisible = !data.isLidClosed
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
            }
        }
    }
}