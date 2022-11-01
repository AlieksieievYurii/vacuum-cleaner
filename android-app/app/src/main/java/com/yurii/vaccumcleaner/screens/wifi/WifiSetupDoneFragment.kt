package com.yurii.vaccumcleaner.screens.wifi

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentWifiSetupDoneBinding
import com.yurii.vaccumcleaner.robot.RobotBluetoothConnection

class WifiSetupDoneFragment : Fragment(R.layout.fragment_wifi_setup_done) {
    private val binding: FragmentWifiSetupDoneBinding by viewBinding()
    private val args: WifiSetupDoneFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ipAddress.text = getString(R.string.label_ip_address, args.ipAddress)
        binding.connect.setOnClickListener {
            RobotBluetoothConnection.closeConnectionIfOpened()
            findNavController().navigate(WifiSetupDoneFragmentDirections.actionWifiSetupDoneToInitialFragment())
        }
    }
}