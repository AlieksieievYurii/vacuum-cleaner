package com.yurii.vaccumcleaner.screens.loading

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.yurii.vaccumcleaner.R
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.databinding.FragmentInitialBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class InitialFragment : Fragment(R.layout.fragment_initial) {
    private val viewModel: InitialFragmentViewModel by viewModels { Injector.provideInitialFragmentViewModel(requireContext()) }
    private val viewBinding: FragmentInitialBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.viewModel = viewModel
        lifecycleScope.launchWhenCreated {
            launch { observeDiscoveryState() }
        }
    }

    private suspend fun observeDiscoveryState() {
        viewModel.state.collectLatest { state ->
            when (state) {
                is InitialFragmentViewModel.State.NotFound -> {
                    viewBinding.status.text = "Cannot find the robot"
                    viewBinding.layoutRobotIsNotFound.isVisible = true
                }
                is InitialFragmentViewModel.State.Scanning -> {
                    viewBinding.status.text = "Discovering..."
                    viewBinding.layoutRobotIsNotFound.isVisible = false
                }
                is InitialFragmentViewModel.State.Connected -> {
                    viewBinding.status.text = "Connected. IP: ${state.ip}"
                    viewBinding.layoutRobotIsNotFound.isVisible = false
                }
            }
        }
    }
}