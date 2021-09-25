package com.yurii.vaccumcleaner.screens.devices

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.yurii.vaccumcleaner.Application
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.BluetoothDevicesFragmentBinding
import com.yurii.vaccumcleaner.observeOnLifecycle

class BluetoothDevicesFragment : Fragment(R.layout.bluetooth_devices_fragment) {
    private val viewModel: BluetoothDevicesViewModel by viewModels { BluetoothDevicesViewModel.Factory(requireActivity().application as Application) }
    private val binding: BluetoothDevicesFragmentBinding by viewBinding()
    private val adapter = Adapter { viewModel.connectBluetoothDevice(it) }

    private val launchBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted)
            viewModel.permissionsAreGranted()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bluetoothDevices.adapter = adapter
        binding.viewModel = viewModel
        requireActivity().registerReceiver(viewModel.broadcastReceiver, BluetoothDevicesViewModel.REQUIRED_BROADCAST_FILTERS)

        viewModel.bluetoothDevices.observeOnLifecycle(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                BluetoothDevicesViewModel.Event.ShowMessageUnableToPair -> Snackbar.make(binding.root, "Unable to pair", Snackbar.LENGTH_LONG).show()
            }
        }
        viewModel.bluetoothState.observeOnLifecycle(viewLifecycleOwner) {
            binding.apply {
                if (it is BluetoothDevicesViewModel.BluetoothState.Ready) {
                    bluetoothDevices.isVisible = true
                    discovering.isVisible = it.isDiscovering
                    search.isVisible = !it.isDiscovering
                }

                layoutEnableBluetooth.isVisible = it == BluetoothDevicesViewModel.BluetoothState.BluetoothIsDisabled
                bluetoothUnsupported.isVisible = it == BluetoothDevicesViewModel.BluetoothState.BluetoothIsUnsupported
                layoutRequestPermissions.isVisible = it == BluetoothDevicesViewModel.BluetoothState.PermissionsDenied
            }
        }

        binding.requestPermission.setOnClickListener {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        binding.enableBluetooth.setOnClickListener {
            launchBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(viewModel.broadcastReceiver)
    }
}