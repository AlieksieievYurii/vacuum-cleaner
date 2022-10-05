package com.yurii.vaccumcleaner.screens.algo

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentAlgorithmSetupBinding
import com.yurii.vaccumcleaner.databinding.ItemChoiceBinding
import com.yurii.vaccumcleaner.databinding.ItemSwitchBinding
import com.yurii.vaccumcleaner.databinding.ItemTextFieldBinding
import com.yurii.vaccumcleaner.robot.ArgumentValue
import com.yurii.vaccumcleaner.robot.AlgorithmParameter
import com.yurii.vaccumcleaner.utils.hideKeyboard
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.ui.LoadingDialog
import java.lang.IllegalStateException

class InputFilterMinMax(private val min: Int, private val max: Int) : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = Integer.parseInt(dest.toString() + source.toString())
            if (isInRange(min, max, input))
                return null
            return ""
        } catch (nfe: NumberFormatException) {
            return ""
        }
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}

class AlgorithmSetupFragment : Fragment(R.layout.fragment_algorithm_setup) {
    private val binding: FragmentAlgorithmSetupBinding by viewBinding()
    private val viewModel: AlgorithmSetupViewModel by viewModels { Injector.provideAlgorithmSetupViewModel() }
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }

    private val argumentsViews = HashMap<AlgorithmParameter, ViewDataBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply.setOnClickListener {
            hideKeyboard()
            onApply()
        }
        binding.algorithms.setOnItemClickListener { _, _, position, _ ->
            viewModel.setAlgorithm(binding.algorithms.adapter.getItem(position).toString())
        }

        viewModel.algorithmNames.observeOnLifecycle(viewLifecycleOwner) {
            it?.run {
                val adapter = ArrayAdapter(requireContext(), R.layout.item_simple_list, it)
                binding.algorithms.setAdapter(adapter)
            }
        }

        viewModel.event.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                AlgorithmSetupViewModel.Event.CloseFragment -> {
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.currentAlgorithm.observeOnLifecycle(viewLifecycleOwner) { script ->
            script?.run {
                binding.algorithms.setText(script.name, false)
                binding.description.isVisible = true
                binding.description.text = script.description
                argumentsViews.clear()
                binding.parametersLayout.removeAllViews()
                script.parameters.forEach {
                    generateParameter(it)
                }

            }
        }

        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner)
    }

    private fun onApply() {
        val parameters = argumentsViews.map { entry ->
            ArgumentValue(
                entry.key.name, value = when (entry.key.getType()) {
                    is AlgorithmParameter.Type.Bool -> (entry.value as ItemSwitchBinding).sw.isChecked
                    is AlgorithmParameter.Type.Floating -> (entry.value as ItemTextFieldBinding).input.text.toString().toFloat()
                    is AlgorithmParameter.Type.IntRange -> (entry.value as ItemTextFieldBinding).input.text.toString().toInt()
                    is AlgorithmParameter.Type.Integer -> (entry.value as ItemTextFieldBinding).input.text.toString().toInt()
                    is AlgorithmParameter.Type.Text -> (entry.value as ItemTextFieldBinding).input.text.toString()
                    is AlgorithmParameter.Type.TextChoice -> (entry.value as ItemChoiceBinding).algorithms.text.toString()
                }
            )
        }
        viewModel.applySettings(parameters)
    }

    private fun generateParameter(argument: AlgorithmParameter) {
        when (val typedValue = argument.getType()) {
            is AlgorithmParameter.Type.Floating -> addEditTextView(argument)
            is AlgorithmParameter.Type.IntRange -> addRangeView(argument, typedValue.from, typedValue.to)
            is AlgorithmParameter.Type.Integer -> addEditTextView(argument)
            is AlgorithmParameter.Type.Text -> addEditTextView(argument)
            is AlgorithmParameter.Type.TextChoice -> addChoiceView(argument, typedValue.values)
            is AlgorithmParameter.Type.Bool -> addSwitchView(argument)
        }
    }

    private fun addSwitchView(argument: AlgorithmParameter) {
        val view: ItemSwitchBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_switch, binding.parametersLayout, true)
        val currentValue = argument.currentValue as Boolean
        view.sw.isChecked = currentValue
        view.sw.text = argument.name
        argumentsViews[argument] = view
    }

    private fun addRangeView(argument: AlgorithmParameter, min: Int, max: Int) {
        val view: ItemTextFieldBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_text_field, binding.parametersLayout, true)
        view.inputLayout.hint = getString(R.string.label_range, argument.name, min, max)
        view.input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        view.input.setText((argument.currentValue as Double).toInt().toString())
        view.input.filters = arrayOf(InputFilterMinMax(min, max))
        argumentsViews[argument] = view
    }

    private fun addChoiceView(argument: AlgorithmParameter, choices: List<String>) {
        val view = DataBindingUtil.inflate<ItemChoiceBinding>(layoutInflater, R.layout.item_choice, binding.parametersLayout, true)
        view.textInputLayout.hint = argument.name
        view.algorithms.setAdapter(ArrayAdapter(requireContext(), R.layout.item_simple_list, choices))
        view.algorithms.setText(argument.currentValue as String, false)
        argumentsViews[argument] = view
    }

    private fun addEditTextView(argument: AlgorithmParameter) {
        val view: ItemTextFieldBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_text_field, binding.parametersLayout, true)
        view.inputLayout.hint = argument.name
        when (val typedValue = argument.getType()) {
            is AlgorithmParameter.Type.Text -> view.input.apply {
                inputType = InputType.TYPE_CLASS_TEXT
                setText(typedValue.value)
            }
            is AlgorithmParameter.Type.Integer -> view.input.apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                setText(typedValue.value.toString())
            }
            is AlgorithmParameter.Type.Floating -> view.input.apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(typedValue.value.toString())
            }
            else -> throw IllegalStateException("Can not add EditTextView for the type ${argument.valueType}")
        }
        argumentsViews[argument] = view
    }

}