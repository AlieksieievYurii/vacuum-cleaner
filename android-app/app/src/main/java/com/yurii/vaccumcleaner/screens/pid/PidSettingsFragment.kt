package com.yurii.vaccumcleaner.screens.pid

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentPidSettingsBinding
import com.yurii.vaccumcleaner.utils.ui.LoadingDialog

class PidSettingsFragment : Fragment(R.layout.fragment_pid_settings) {
    private val viewModel: PidSettingsViewModel by viewModels { Injector.providePidSettingsViewModel() }
    private val binding: FragmentPidSettingsBinding by viewBinding()
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner)
    }
}