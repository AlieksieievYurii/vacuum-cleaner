package com.yurii.vaccumcleaner.screens.algo

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.ArrayAdapter
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
import com.yurii.vaccumcleaner.robot.ScriptArgument
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.ui.LoadingDialog
import timber.log.Timber
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

    private val argumentsViews = HashMap<ScriptArgument, ViewDataBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply.setOnClickListener { onApply() }
        binding.scripts.setOnItemClickListener { _, _, position, _ ->
            viewModel.setScript(binding.scripts.adapter.getItem(position).toString())
        }

        viewModel.scriptsList.observeOnLifecycle(viewLifecycleOwner) {
            it?.run {
                val adapter = ArrayAdapter(requireContext(), R.layout.item_simple_list, it)
                binding.scripts.setAdapter(adapter)
            }
        }

        viewModel.event.observeOnLifecycle(viewLifecycleOwner) { event ->
            when(event) {
                AlgorithmSetupViewModel.Event.CloseFragment -> findNavController().popBackStack()
            }
        }

        viewModel.currentScript.observeOnLifecycle(viewLifecycleOwner) { script ->
            script?.run {
                binding.scripts.setText(script.name, false)
                binding.description.text = script.description
                argumentsViews.clear()
                binding.argumentsLayout.removeAllViews()
                script.arguments.forEach {
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
                    is ScriptArgument.Type.Bool -> (entry.value as ItemSwitchBinding).sw.isChecked
                    is ScriptArgument.Type.Floating -> (entry.value as ItemTextFieldBinding).input.text.toString().toFloat()
                    is ScriptArgument.Type.IntRange -> (entry.value as ItemTextFieldBinding).input.text.toString().toInt()
                    is ScriptArgument.Type.Integer -> (entry.value as ItemTextFieldBinding).input.text.toString().toInt()
                    is ScriptArgument.Type.Text -> (entry.value as ItemTextFieldBinding).input.text.toString()
                    is ScriptArgument.Type.TextChoice -> (entry.value as ItemChoiceBinding).scripts.text.toString()
                }
            )
        }
        viewModel.applySettings(parameters)
    }

    private fun generateParameter(argument: ScriptArgument) {
        when (val typedValue = argument.getType()) {
            is ScriptArgument.Type.Floating -> addEditTextView(argument)
            is ScriptArgument.Type.IntRange -> addRangeView(argument, typedValue.from, typedValue.to)
            is ScriptArgument.Type.Integer -> addEditTextView(argument)
            is ScriptArgument.Type.Text -> addEditTextView(argument)
            is ScriptArgument.Type.TextChoice -> addChoiceView(argument, typedValue.values)
            is ScriptArgument.Type.Bool -> addSwitchView(argument)
        }
    }

    private fun addSwitchView(argument: ScriptArgument) {
        val view: ItemSwitchBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_switch, binding.argumentsLayout, true)
        val currentValue = argument.currentValue as Boolean
        view.sw.isChecked = currentValue
        view.sw.text = argument.name
        argumentsViews[argument] = view
    }

    private fun addRangeView(argument: ScriptArgument, min: Int, max: Int) {
        val view: ItemTextFieldBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_text_field, binding.argumentsLayout, true)
        view.inputLayout.hint = getString(R.string.label_range, argument.name, min, max)
        view.input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        view.input.setText((argument.currentValue as Double).toInt().toString())
        view.input.filters = arrayOf(InputFilterMinMax(min, max))
        argumentsViews[argument] = view
    }

    private fun addChoiceView(argument: ScriptArgument, choices: List<String>) {
        val view = DataBindingUtil.inflate<ItemChoiceBinding>(layoutInflater, R.layout.item_choice, binding.argumentsLayout, true)
        view.textInputLayout.hint = argument.name
        view.scripts.setAdapter(ArrayAdapter(requireContext(), R.layout.item_simple_list, choices))
        view.scripts.setText(argument.currentValue as String, false)
        argumentsViews[argument] = view
    }

    private fun addEditTextView(argument: ScriptArgument) {
        val view: ItemTextFieldBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_text_field, binding.argumentsLayout, true)
        view.inputLayout.hint = argument.name
        when (val typedValue = argument.getType()) {
            is ScriptArgument.Type.Text -> view.input.apply {
                inputType = InputType.TYPE_CLASS_TEXT
                setText(typedValue.value)
            }
            is ScriptArgument.Type.Integer -> view.input.apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                setText(typedValue.value.toString())
            }
            is ScriptArgument.Type.Floating -> view.input.apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(typedValue.value.toString())
            }
            else -> throw IllegalStateException("Can not add EditTextView for the type ${argument.valueType}")
        }
        argumentsViews[argument] = view
    }

}