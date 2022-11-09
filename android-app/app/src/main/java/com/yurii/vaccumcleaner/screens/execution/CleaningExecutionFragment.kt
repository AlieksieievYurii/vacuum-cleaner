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
import com.yurii.vaccumcleaner.robot.PauseStopReason
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.ui.showError

class CleaningExecutionFragment : Fragment(R.layout.fragment_cleaning_execution) {
    private val binding: FragmentCleaningExecutionBinding by viewBinding()
    private val viewModel: CleaningExecutionViewModel by viewModels { Injector.provideCleaningExecutionViewModel() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.pauseReason.observeOnLifecycle(viewLifecycleOwner) { pauseReason ->
            binding.pauseReason.apply {
                when (pauseReason) {
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
            }
        }

        viewModel.event.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                CleaningExecutionViewModel.Event.NavigateToPanelFragment -> findNavController().navigate(R.id.action_cleaningExecutionFragment_to_panelFragment)
                is CleaningExecutionViewModel.Event.ShowError -> showError(binding.root, getString(R.string.label_error_occurred), event.exception)
            }
        }
    }
}