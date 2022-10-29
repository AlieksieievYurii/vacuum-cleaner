package com.yurii.vaccumcleaner.screens.binder

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentBinderBinding
import com.yurii.vaccumcleaner.screens.devices.BluetoothDevicesViewModel
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import com.yurii.vaccumcleaner.utils.runAnimation

class BinderFragment : Fragment(R.layout.fragment_binder) {
    private val binding: FragmentBinderBinding by viewBinding()
    private val viewModel: BinderViewModel by viewModels { Injector.provideBinderViewModel(requireContext()) }
    private val launchBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted)
            viewModel.bluetoothPermissionsAreGranted()
    }

    private var currentPlayingAnimationId = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().registerReceiver(viewModel.broadcastReceiver, BluetoothDevicesViewModel.REQUIRED_BROADCAST_FILTERS)

        binding.apply {
            findRobot.setOnClickListener { viewModel.startDiscoveringRobot() }
            turnOnBluetooth.setOnClickListener { launchBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }
            giveAccess.setOnClickListener { locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            rescan.setOnClickListener { viewModel.rescan() }
            pair.setOnClickListener { viewModel.askToPair() }
            retryPairing.setOnClickListener { viewModel.retryPairing() }
        }

        viewModel.currentState.observeOnLifecycle(viewLifecycleOwner) { state ->
            updateLayoutVisibilityDependingOnState(state)

            when (state) {
                BinderViewModel.State.BluetoothIsDisabled -> runSafelyAnimation(R.raw.failed, infinitive = false)
                is BinderViewModel.State.Discovering -> {
                    runSafelyAnimation(R.raw.bluetooth_scanning)
                    binding.discoveringInfo.text = getString(R.string.label_discovering_info, state.found)
                }
                BinderViewModel.State.Initial -> runSafelyAnimation(R.raw.required_connection)
                BinderViewModel.State.PermissionDenied -> runSafelyAnimation(R.raw.failed, infinitive = false)
                BinderViewModel.State.RobotIsPairing -> runSafelyAnimation(R.raw.bluetooth_scanning, infinitive = false)
                BinderViewModel.State.RobotNeedsToBePaired -> runSafelyAnimation(R.raw.info)
                BinderViewModel.State.RobotNotFound -> runSafelyAnimation(R.raw.failed, infinitive = false)
                BinderViewModel.State.RobotPaired -> runSafelyAnimation(R.raw.done, infinitive = false)
                BinderViewModel.State.RobotPairingFailed -> runSafelyAnimation(R.raw.failed, infinitive = false)
            }
        }

        viewModel.event.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                BinderViewModel.Event.NavigateToWifiSettingsScreen -> findNavController().navigate(R.id.action_binderFragment_to_wifiSettingsFragment)
            }
        }
    }

    private fun updateLayoutVisibilityDependingOnState(state: BinderViewModel.State) {
        binding.apply {
            layoutRobotRequiresToBePaired.isVisible = state is BinderViewModel.State.RobotNeedsToBePaired
            layoutRobotIsPairing.isVisible = state is BinderViewModel.State.RobotIsPairing
            layoutInit.isVisible = state is BinderViewModel.State.Initial
            layoutDiscoveringRobot.isVisible = state is BinderViewModel.State.Discovering
            layoutRequestPermissions.isVisible = state is BinderViewModel.State.PermissionDenied
            layoutTurnOnBluetooth.isVisible = state is BinderViewModel.State.BluetoothIsDisabled
            layoutRobotIsNotFound.isVisible = state is BinderViewModel.State.RobotNotFound
            layoutRobotIsPaired.isVisible = state is BinderViewModel.State.RobotPaired
            layoutRobotPairingFailed.isVisible = state is BinderViewModel.State.RobotPairingFailed
        }
    }

    private fun runSafelyAnimation(animationResId: Int, infinitive: Boolean = true) {
        if (animationResId != currentPlayingAnimationId) {
            binding.animationView.runAnimation(animationResId, infinitive = infinitive)
            currentPlayingAnimationId = animationResId
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(viewModel.broadcastReceiver)
    }
}