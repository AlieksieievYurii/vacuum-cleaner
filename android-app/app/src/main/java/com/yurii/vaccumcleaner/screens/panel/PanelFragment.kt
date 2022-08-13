package com.yurii.vaccumcleaner.screens.panel

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentPanelBinding
import com.yurii.vaccumcleaner.requesthandler.RequestHandler
import com.yurii.vaccumcleaner.robot.WifiCommunicator
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

@JsonClass(generateAdapter = true)
data class TestRequest(
    val name: String,
    val age: Int
)

@JsonClass(generateAdapter = true)
data class TestResponse(
    @Json(name = "passport_id") val passwordId: String,
    @Json(name = "dir_out") val dirOut: String
)

class PanelFragment : Fragment(R.layout.fragment_panel) {
    private val viewModel: PanelViewModel by viewModels { Injector.providePanelViewModel() }
    private val binding: FragmentPanelBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        val wifiCommunicator = WifiCommunicator()
        val requestHandler = RequestHandler(wifiCommunicator, lifecycleScope)
        lifecycleScope.launch(Dispatchers.IO) {
            wifiCommunicator.connect("192.168.18.2", 1488)
            requestHandler.start()
            val r = requestHandler.send("/hello-world", TestRequest("aa", 1), TestResponse::class.java, 10000)
            Timber.d(r.toString())
        }


        viewModel.event.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                PanelViewModel.Event.NavigateToControlFragment -> findNavController().navigate(R.id.action_panelFragment_to_manualControlFragment)
            }
        }
    }
}