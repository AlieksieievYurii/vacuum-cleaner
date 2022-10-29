package com.yurii.vaccumcleaner.screens.loading

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.yurii.vaccumcleaner.R
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.databinding.FragmentInitialBinding
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.runAnimation


class InitialFragment : Fragment(R.layout.fragment_initial) {
    private val viewModel: InitialFragmentViewModel by viewModels { Injector.provideInitialFragmentViewModel(requireContext()) }
    private val viewBinding: FragmentInitialBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.viewModel = viewModel
        observeDiscoveryState()
        observeEvents()
    }

    private fun observeDiscoveryState() {
        viewModel.state.observeOnLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is InitialFragmentViewModel.State.NotFound -> {
                    viewBinding.animationView.runAnimation(R.raw.failed)
                    viewBinding.status.setText(R.string.label_can_not_find_robot)
                    viewBinding.layoutRobotIsNotFound.isVisible = true
                }
                is InitialFragmentViewModel.State.Scanning -> {
                    viewBinding.status.setText(R.string.label_discovering)
                    viewBinding.animationView.runAnimation(R.raw.scanning, infinitive = true)
                    viewBinding.layoutRobotIsNotFound.isVisible = false
                }
                is InitialFragmentViewModel.State.Connected -> {
                    viewBinding.status.text = getString(R.string.label_connected_with_ip, state.ip)
                    viewBinding.layoutRobotIsNotFound.isVisible = false
                    viewBinding.animationView.runAnimation(R.raw.done)
                }
            }
        }
    }

    private fun observeEvents() {
        viewModel.event.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                InitialFragmentViewModel.Event.NavigateToBindRobot -> findNavController().navigate(R.id.action_initialFragment_to_binderFragment)
                InitialFragmentViewModel.Event.NavigateToControlPanel -> findNavController().navigate(R.id.action_initialFragment_to_panelFragment)
                InitialFragmentViewModel.Event.NavigateToExecutionScreen -> findNavController().navigate(R.id.action_initialFragment_to_cleaningExecutionFragment)
            }
        }
    }
}