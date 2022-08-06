package com.yurii.vaccumcleaner.screens.binder

import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentBinderBinding

class BinderFragment : Fragment(R.layout.fragment_binder) {
    private val binding: FragmentBinderBinding by viewBinding()
    private val viewModel: BinderViewModel by viewModels { Injector.provideBinderViewModel() }
}