package com.yurii.vaccumcleaner.screens.wifi

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.yurii.vaccumcleaner.MainViewModel
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentWifiSettingsBinding
import com.yurii.vaccumcleaner.observeOnLifecycle


class WifiSettingsFragment : Fragment(R.layout.fragment_wifi_settings) {
    private val viewModel: WifiSettingsViewModel by viewModels {
        WifiSettingsViewModel.Factory(
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java).bluetoothService
        )
    }
    private val binding: FragmentWifiSettingsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.state.observeOnLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is WifiSettingsViewModel.State.Error -> binding.apply {
                    loading.isVisible = false
                    image.isVisible = true
                    image.setImageResource(R.drawable.ic_error)
                    hint.isVisible = true
                    hint.text = getString(R.string.label_error_message, state.message)
                    inputState(isEnabled = true)
                }
                WifiSettingsViewModel.State.LoadingWifiSettings -> binding.apply {
                    loading.isVisible = true
                    image.isVisible = false
                    hint.text = getString(R.string.hint_loading_cuurent_configuration)
                    inputState(isEnabled = false)
                }
                WifiSettingsViewModel.State.None -> binding.apply {
                    hint.isVisible = false
                    loading.isVisible = false
                    image.isVisible = true
                    image.setImageResource(R.drawable.ic_wifi)
                    inputState(isEnabled = true)
                }
                WifiSettingsViewModel.State.ApplyingWifiSettings -> binding.apply {
                    loading.isVisible = true
                    image.isVisible = false
                    hint.isVisible = true
                    hint.text = getString(R.string.hint_applying_wifi_settings)
                    inputState(isEnabled = false)
                }
            }
        }

        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                WifiSettingsViewModel.Event.SettingsHaveBeenApplied -> Toast.makeText(
                    requireContext(),
                    "Wifi setup has been applied!",
                    Toast.LENGTH_SHORT
                ).show()
                WifiSettingsViewModel.Event.CurrentSetupHasBeenReturned -> Toast.makeText(
                    requireContext(),
                    "Current Wifi setup has been returned",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun inputState(isEnabled: Boolean) {
        binding.apply {
            ssid.isEnabled = isEnabled
            password.isEnabled = isEnabled
            apply.isEnabled = isEnabled
        }
    }
}