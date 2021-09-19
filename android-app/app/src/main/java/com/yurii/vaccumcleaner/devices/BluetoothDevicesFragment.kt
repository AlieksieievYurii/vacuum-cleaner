package com.yurii.vaccumcleaner.devices

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.BluetoothDevicesFragmentBinding

class BluetoothDevicesFragment : Fragment(R.layout.bluetooth_devices_fragment) {
    private val binding: BluetoothDevicesFragmentBinding by viewBinding()
    private val adapter = Adapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bluetoothDevices.adapter = adapter
        adapter.submitList(listOf(BluetoothDeviceItem("Test", "2344", isPaired = true)))
    }
}