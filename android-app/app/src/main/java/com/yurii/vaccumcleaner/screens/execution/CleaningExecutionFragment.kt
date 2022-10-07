package com.yurii.vaccumcleaner.screens.execution

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentCleaningExecutionBinding
import com.yurii.vaccumcleaner.screens.algo.AlgorithmSetupViewModel

class CleaningExecutionFragment : Fragment(R.layout.fragment_cleaning_execution) {
    private val binding: FragmentCleaningExecutionBinding by viewBinding()
    private val viewModel: AlgorithmSetupViewModel by viewModels { Injector.provideAlgorithmSetupViewModel() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}