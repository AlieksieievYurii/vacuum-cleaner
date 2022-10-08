package com.yurii.vaccumcleaner.screens.panel

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentPanelBinding
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.ui.LoadingDialog
import com.yurii.vaccumcleaner.utils.ui.showError

class PanelFragment : Fragment(R.layout.fragment_panel) {
    private val viewModel: PanelViewModel by viewModels { Injector.providePanelViewModel() }
    private val binding: FragmentPanelBinding by viewBinding()
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.headerWidget.observeBatteryState(viewModel.batteryState, viewLifecycleOwner)
        viewModel.lidIsOpened.observeOnLifecycle(viewLifecycleOwner) { isLidOpened -> binding.headerWidget.setLidStatus(isLidOpened) }
        viewModel.dustBoxIsOut.observeOnLifecycle(viewLifecycleOwner) { isDustBoxOut -> binding.headerWidget.setDustBoxStatus(isDustBoxOut) }
        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner)

        viewModel.event.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                PanelViewModel.Event.NavigateToControlFragment -> findNavController().navigate(R.id.action_panelFragment_to_manualControlFragment)
                is PanelViewModel.Event.ShowError -> showError(binding.root, getString(R.string.label_error_occurred), it.exception)
                PanelViewModel.Event.NavigateToPidSettingsFragment -> findNavController().navigate(R.id.action_panelFragment_to_pidSettingsFragment)
                PanelViewModel.Event.NavigateToAlgorithmSetupFragment -> findNavController().navigate(R.id.action_panelFragment_to_algorithmSetupFragment)
                PanelViewModel.Event.NavigateToCleaningExecutionFragment -> findNavController().navigate(R.id.action_panelFragment_to_cleaningExecutionFragment)
            }
        }
    }
}