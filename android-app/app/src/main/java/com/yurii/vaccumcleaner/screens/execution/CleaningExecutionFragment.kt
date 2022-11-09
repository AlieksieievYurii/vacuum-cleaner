package com.yurii.vaccumcleaner.screens.execution

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.vaccumcleaner.utils.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentCleaningExecutionBinding
import com.yurii.vaccumcleaner.robot.CleaningStatusEnum
import com.yurii.vaccumcleaner.robot.PauseStopReason
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.ui.LoadingDialog
import com.yurii.vaccumcleaner.utils.ui.showError

class CleaningExecutionFragment : Fragment(R.layout.fragment_cleaning_execution) {
    private val binding: FragmentCleaningExecutionBinding by viewBinding()
    private val viewModel: CleaningExecutionViewModel by viewModels { Injector.provideCleaningExecutionViewModel() }
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner)

        binding.headerWidget.observeBatteryState(viewModel.batteryState, viewLifecycleOwner)

        viewModel.lidIsOpened.observeOnLifecycle(viewLifecycleOwner) { isLidOpened ->
            binding.headerWidget.setLidStatus(isLidOpened)
            binding.pauseOrResumeButton.isEnabled = !isLidOpened && !viewModel.dustBoxIsOut.value
        }

        viewModel.dustBoxIsOut.observeOnLifecycle(viewLifecycleOwner) { isDustBoxOut ->
            binding.headerWidget.setDustBoxStatus(isDustBoxOut)
            binding.pauseOrResumeButton.isEnabled = !isDustBoxOut && !viewModel.lidIsOpened.value
        }

        binding.pauseOrResumeButton.setOnClickListener { viewModel.pauseOrResume() }
        binding.finish.setOnClickListener { viewModel.stopCleaning() }

        observerCleaningStatus()
        observerEvents()
    }

    private fun observerCleaningStatus() {
        viewModel.cleaningStatus.observeOnLifecycle(viewLifecycleOwner) { cleaningStatus ->
            if (cleaningStatus == null)
                return@observeOnLifecycle

            binding.algorithmName.text = getString(R.string.label_algorithm_name, cleaningStatus.cleaningInfo?.algorithmName)
            binding.startTime.text = getString(R.string.label_start_time, cleaningStatus.cleaningInfo?.timestamp)

            binding.pauseOrResumeButton.apply {
                if (cleaningStatus.status == CleaningStatusEnum.RUNNING) {
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_24, 0, 0, 0)
                    text = getString(R.string.label_pause)

                } else if (cleaningStatus.status == CleaningStatusEnum.PAUSED) {
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_24, 0, 0, 0)
                    text = getString(R.string.label_resume)
                }
            }

            binding.pauseReason.apply {
                if (cleaningStatus.status == CleaningStatusEnum.PAUSED) {

                    when (cleaningStatus.reason) {
                        PauseStopReason.LID_IS_OPENED -> {
                            visibility = View.VISIBLE
                            text = getString(R.string.label_lid_opened)
                        }
                        PauseStopReason.DUST_BOX_OUT -> {
                            visibility = View.VISIBLE
                            text = getString(R.string.label_box_out)
                        }
                        else -> {
                            visibility = View.INVISIBLE
                        }
                    }
                } else
                    visibility = View.INVISIBLE
            }
        }
    }

    private fun observerEvents() {
        viewModel.event.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                CleaningExecutionViewModel.Event.NavigateToPanelFragment -> findNavController().navigate(R.id.action_cleaningExecutionFragment_to_panelFragment)
                is CleaningExecutionViewModel.Event.ShowError -> showError(binding.root, getString(R.string.label_error_occurred), event.exception)
            }
        }
    }
}