package com.yurii.vaccumcleaner.screens.wifi

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.vaccumcleaner.utils.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentWifiSettingsBinding
import com.yurii.vaccumcleaner.utils.hideKeyboard
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.ui.LoadingDialog
import com.yurii.vaccumcleaner.utils.ui.showError

import android.widget.ArrayAdapter


class WifiSettingsFragment : Fragment(R.layout.fragment_wifi_settings) {
    private val viewModel: WifiSettingsViewModel by viewModels { Injector.provideWifiSettingsViewModel(bluetoothProvider = true) }
    private val binding: FragmentWifiSettingsBinding by viewBinding()
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.apply.setOnClickListener {
            viewModel.applyWifiSettings()
            hideKeyboard()
        }

        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner)

        viewModel.availableAccessPoints.observeOnLifecycle(viewLifecycleOwner) {
            val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, it)
            binding.ssid.setAdapter(adapter)
        }

        viewModel.event.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                is WifiSettingsViewModel.Event.ShowError -> showError(binding.root, getString(R.string.label_error_occurred), event.error)
                is WifiSettingsViewModel.Event.NavigateToWifiSetupDoneScreen -> findNavController().navigate(
                    WifiSettingsFragmentDirections.actionWifiSettingsFragmentToWifiSetupDone(event.deviceIpAddress)
                )
            }
        }
    }
}