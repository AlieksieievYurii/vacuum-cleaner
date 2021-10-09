package com.yurii.vaccumcleaner.screens.debug

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentDebugBinding
import com.yurii.vaccumcleaner.observeOnLifecycle

class DebugFragment : Fragment(R.layout.fragment_debug) {
    private val binding: FragmentDebugBinding by viewBinding()
    private val args: DebugFragmentArgs by navArgs()
    private val viewModel: DebugViewModel by viewModels { DebugViewModel.Factory(args.bluetoothDevice) }
    private val adapter: Adapter by lazy {
        Adapter { packet ->

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.packets.adapter = adapter
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

        viewModel.packets.observeOnLifecycle(viewLifecycleOwner) { adapter.submitList(it) }
    }
}