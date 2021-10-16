package com.yurii.vaccumcleaner.screens.settings

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yurii.vaccumcleaner.MainViewModel
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentSettingsBinding
import com.yurii.vaccumcleaner.observeOnLifecycle

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val binding: FragmentSettingsBinding by viewBinding()
    private val args: SettingsFragmentArgs by navArgs()
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            args.bluetoothDevice, ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.bluetoothStatus.observeOnLifecycle(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    BluetoothStatus.DISCONNECTED -> {
                        bluetoothStatusIndicator.setBackgroundResource(R.drawable.circle_red)
                        bluetoothStatus.text = getString(R.string.label_disconnected)
                    }
                    BluetoothStatus.CONNECTING -> {
                        bluetoothStatusIndicator.setBackgroundResource(R.drawable.circle_yellow)
                        bluetoothStatus.text = getString(R.string.label_connecting)
                    }
                    BluetoothStatus.CONNECTED -> {
                        bluetoothStatusIndicator.setBackgroundResource(R.drawable.circle_green)
                        bluetoothStatus.text = getString(R.string.label_connected)
                    }
                }
            }
        }

        viewModel.serviceConnectionStatus.observeOnLifecycle(viewLifecycleOwner) {
            binding.apply {
                when(it) {
                    ServiceConnectionStatus.VALIDATED -> {
                        serviceStatusIndicator.setBackgroundResource(R.drawable.circle_green)
                        serviceStatus.text = getString(R.string.label_validated)
                    }
                    ServiceConnectionStatus.VALIDATING -> {
                        serviceStatusIndicator.setBackgroundResource(R.drawable.circle_yellow)
                        serviceStatus.text = getString(R.string.label_validating)
                    }
                    ServiceConnectionStatus.BROKEN -> {
                        serviceStatusIndicator.setBackgroundResource(R.drawable.circle_red)
                        serviceStatus.text = getString(R.string.label_broken)
                    }
                }
            }
        }

        binding.setWifi.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToWifiSettingsFragment())
        }

        binding.debug.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToDebugFragment(args.bluetoothDevice))
        }
    }
}