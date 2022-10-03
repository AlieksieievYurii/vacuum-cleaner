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
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentAlgorithmSetupBinding
import com.yurii.vaccumcleaner.databinding.ItemChoiceBinding
import com.yurii.vaccumcleaner.databinding.ItemSwitchBinding
import com.yurii.vaccumcleaner.databinding.ItemTextFieldBinding
import com.yurii.vaccumcleaner.robot.ScriptArgument
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
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

    private val argumentView = HashMap<ScriptArgument, ViewDataBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.scriptsList.observeOnLifecycle(viewLifecycleOwner) {
            it?.run {
                val adapter = ArrayAdapter(requireContext(), R.layout.item_simple_list, it)
                binding.scripts.setAdapter(adapter)
            }
        }

        viewModel.currentScript.observeOnLifecycle(viewLifecycleOwner) { script ->
            script?.run {
                binding.scripts.setText(script.name, false)
                binding.description.text = script.description
                argumentView.clear()
                script.arguments.forEach {
                    generateParameter(it)
                }

            }
        }
    }

    fun onApply() {

    }

    private fun generateParameter(argument: ScriptArgument) {
        if (argument.valueType in arrayOf("string", "integer", "floating"))
            addEditTextView(argument)
        else if (argument.valueType == "boolean")
            addSwitchView(argument)
        else {
            val rangeTypeRegex = Regex("(\\d+)..(\\d+)")
            val choiceTypeRegex = Regex("([^,]+)")
            if (rangeTypeRegex.containsMatchIn(argument.valueType)) {
                val (min, max) = rangeTypeRegex.find(argument.valueType)!!.destructured
                addRangeView(argument, min.toInt(), max.toInt())
            } else if (choiceTypeRegex.containsMatchIn(argument.valueType)) {
                val choices = choiceTypeRegex.findAll(argument.valueType).toList().map { it.value }
                addChoiceView(argument, choices)
            }
        }
    }

    private fun addSwitchView(argument: ScriptArgument) {
        val view: ItemSwitchBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_switch, binding.argumentsLayout, true)
        val currentValue = argument.currentValue as Boolean
        view.sw.isChecked = currentValue
        view.sw.text = argument.name
        argumentView[argument] = view
    }

    private fun addRangeView(argument: ScriptArgument, min: Int, max: Int) {
        val view: ItemTextFieldBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_text_field, binding.argumentsLayout, true)
        view.inputLayout.hint = getString(R.string.label_range, argument.name, min, max)
        view.input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        view.input.setText((argument.currentValue as Double).toInt().toString())
        view.input.filters = arrayOf(InputFilterMinMax(min, max))
        argumentView[argument] = view
    }

    private fun addChoiceView(argument: ScriptArgument, choices: List<String>) {
        val view = DataBindingUtil.inflate<ItemChoiceBinding>(layoutInflater, R.layout.item_choice, binding.argumentsLayout, true)
        view.textInputLayout.hint = argument.name
        view.scripts.setAdapter(ArrayAdapter(requireContext(), R.layout.item_simple_list, choices))
        view.scripts.setText(argument.currentValue as String, false)
        argumentView[argument] = view
    }

    private fun addEditTextView(argument: ScriptArgument) {
        val view: ItemTextFieldBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_text_field, binding.argumentsLayout, true)
        view.inputLayout.hint = argument.name
        when (argument.valueType) {
            "string" -> view.input.apply {
                inputType = InputType.TYPE_CLASS_TEXT
                setText(argument.currentValue as String)
            }
            "integer" -> view.input.apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                setText((argument.currentValue as Double).toInt().toString())
            }
            "floating" -> view.input.apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText((argument.currentValue as Double).toString())
            }
            else -> throw IllegalStateException("Can not add EditTextView for the type ${argument.valueType}")
        }
        argumentView[argument] = view
    }

}