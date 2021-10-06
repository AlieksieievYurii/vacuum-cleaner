package com.yurii.vaccumcleaner.screens.debug

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentDebugBinding

class DebugFragment : Fragment(R.layout.fragment_debug) {
    private val binding: FragmentDebugBinding by viewBinding()
    private val args: DebugFragmentArgs by navArgs()
    private val viewModel: DebugViewModel by viewModels { DebugViewModel.Factory(args.bluetoothDevice) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel
    }
}