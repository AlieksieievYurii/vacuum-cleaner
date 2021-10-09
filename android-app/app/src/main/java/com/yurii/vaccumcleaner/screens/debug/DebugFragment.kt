package com.yurii.vaccumcleaner.screens.debug

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.*
import com.yurii.vaccumcleaner.observeOnLifecycle

class DebugFragment : Fragment(R.layout.fragment_debug) {
    private val binding: FragmentDebugBinding by viewBinding()
    private val args: DebugFragmentArgs by navArgs()
    private val viewModel: DebugViewModel by viewModels { DebugViewModel.Factory(args.bluetoothDevice) }
    private val adapter: Adapter by lazy { Adapter(this::showDialog) }

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

    private fun showDialog(packet: Packet) {
        val title: String
        val view: ViewBinding
        when (packet) {
            is Packet.Broken -> {
                title = getString(R.string.label_broken)
                view = DataBindingUtil.inflate<DialogDebugBrokenDetailBinding>(layoutInflater, R.layout.dialog_debug_broken_detail, null, false)
                    .apply {
                        broken = packet
                    }

            }
            is Packet.Request -> {
                title = getString(R.string.label_request)
                view = DataBindingUtil.inflate<DialogDebugRequestDetailBinding>(layoutInflater, R.layout.dialog_debug_request_detail, null, false)
                    .apply {
                        request = packet
                    }
            }
            is Packet.Response -> {
                title = getString(R.string.label_response)
                view = DataBindingUtil.inflate<DialogDebugResponseDetailBinding>(layoutInflater, R.layout.dialog_debug_response_detail, null, false)
                    .apply {
                        response = packet
                    }
            }
        }
        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(title)
            setView(view.root)

        }.create()

        dialog.show()
    }
}