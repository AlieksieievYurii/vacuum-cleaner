package com.yurii.vaccumcleaner.devices

import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.BluetoothDevicesFragmentBinding
import com.yurii.vaccumcleaner.observeOnLifecycle

class BluetoothDevicesFragment : Fragment(R.layout.bluetooth_devices_fragment) {
    private val viewModel: BluetoothDevicesViewModel by viewModels { BluetoothDevicesViewModel.Factory() }
    private val binding: BluetoothDevicesFragmentBinding by viewBinding()
    private val adapter = Adapter(viewModel::connectBluetoothDevice)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bluetoothDevices.adapter = adapter
        requireActivity().registerReceiver(viewModel.broadcastReceiverBluetoothDeviceFound, IntentFilter(BluetoothDevice.ACTION_FOUND))

        viewModel.bluetoothDevices.observeOnLifecycle(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.bluetoothState.observeOnLifecycle(viewLifecycleOwner) {
            binding.apply {
                bluetoothDevices.isVisible = it == BluetoothDevicesViewModel.BluetoothState.None
                bluetoothUnsupported.isVisible = it == BluetoothDevicesViewModel.BluetoothState.BluetoothIsUnsupported
                bluetoothTurnedOff.isVisible = it == BluetoothDevicesViewModel.BluetoothState.BluetoothIsDisabled
                enableBluetooth.isVisible = it == BluetoothDevicesViewModel.BluetoothState.BluetoothIsDisabled
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(viewModel.broadcastReceiverBluetoothDeviceFound)
    }
}