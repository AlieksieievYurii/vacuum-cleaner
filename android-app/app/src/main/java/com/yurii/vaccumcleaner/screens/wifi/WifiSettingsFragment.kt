package com.yurii.vaccumcleaner.screens.wifi

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentWifiSettingsBinding

class WifiSettingsFragment : Fragment(R.layout.fragment_wifi_settings) {
    private val binding: FragmentWifiSettingsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}