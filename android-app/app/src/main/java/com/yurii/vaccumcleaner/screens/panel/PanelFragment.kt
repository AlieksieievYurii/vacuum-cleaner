package com.yurii.vaccumcleaner.screens.panel

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentPanelBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class PanelFragment : Fragment(R.layout.fragment_panel) {
    private val viewModel: PanelViewModel by viewModels { Injector.providePanelViewModel() }
    private val binding: FragmentPanelBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        lifecycleScope.launchWhenCreated {
            viewModel.event.collect {
                when (it) {
                    PanelViewModel.Event.NavigateToControlFragment -> findNavController().navigate(R.id.action_panelFragment_to_manualControlFragment)
                }
            }
        }
    }
}