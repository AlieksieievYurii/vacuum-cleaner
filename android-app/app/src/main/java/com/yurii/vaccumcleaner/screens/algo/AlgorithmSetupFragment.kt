package com.yurii.vaccumcleaner.screens.algo

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentAlgorithmSetupBinding

class AlgorithmSetupFragment : Fragment(R.layout.fragment_algorithm_setup) {
    private val binding: FragmentAlgorithmSetupBinding by viewBinding()
    private val viewModel: AlgorithmSetupViewModel by viewModels { Injector.provideAlgorithmSetupViewModel() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val items = listOf("Option 1", "Option 2", "Option 3", "Option 4")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_simple_list, items)
        binding.scripts.setAdapter(adapter)

        viewModel.test()
    }
}