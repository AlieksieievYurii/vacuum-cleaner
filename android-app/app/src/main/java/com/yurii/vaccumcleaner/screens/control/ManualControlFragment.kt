package com.yurii.vaccumcleaner.screens.control

import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentControlBinding

class ManualControlFragment : Fragment(R.layout.fragment_control) {
    private val binding: FragmentControlBinding by viewBinding()
    private val viewModel: ManualControlViewModel by viewModels { Injector.provideManualControlViewModel() }
}