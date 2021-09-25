package com.yurii.vaccumcleaner.screens.settings

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val binding: FragmentSettingsBinding by viewBinding()
    private val args: SettingsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setWifi.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToWifiSettingsFragment(args.bluetoothDevice))
        }
    }
}