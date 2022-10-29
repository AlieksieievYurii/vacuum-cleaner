package com.yurii.vaccumcleaner.screens.wifi

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentWifiSettingsBinding
import com.yurii.vaccumcleaner.utils.ui.LoadingDialog


class WifiSettingsFragment : Fragment(R.layout.fragment_wifi_settings) {
    private val viewModel: WifiSettingsViewModel by viewModels { Injector.provideWifiSettingsViewModel(bluetoothProvider = true) }
    private val binding: FragmentWifiSettingsBinding by viewBinding()
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner)
    }
}