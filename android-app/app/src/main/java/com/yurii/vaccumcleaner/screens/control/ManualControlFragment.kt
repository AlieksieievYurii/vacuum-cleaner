package com.yurii.vaccumcleaner.screens.control

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentControlBinding
import com.yurii.vaccumcleaner.utils.setPressedUnpressedListener

class ManualControlFragment : Fragment(R.layout.fragment_control) {
    private val binding: FragmentControlBinding by viewBinding()
    private val viewModel: ManualControlViewModel by viewModels { Injector.provideManualControlViewModel(requireActivity()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel

        binding.btnForward.setPressedUnpressedListener(onPress = viewModel::moveForward, onRelease = viewModel::stop)
        binding.btnBackward.setPressedUnpressedListener(onPress = viewModel::moveBackward, onRelease = viewModel::stop)
        binding.btnLeft.setPressedUnpressedListener(onPress = viewModel::turnLeft, onRelease = viewModel::stop)
        binding.btnRight.setPressedUnpressedListener(onPress = viewModel::turnRight, onRelease = viewModel::stop)


    }
}