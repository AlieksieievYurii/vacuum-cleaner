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
import com.yurii.vaccumcleaner.Injector
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.FragmentBinderBinding
import com.yurii.vaccumcleaner.screens.devices.BluetoothDevicesViewModel
import com.yurii.vaccumcleaner.utils.observeOnLifecycle
import timber.log.Timber

class BinderFragment : Fragment(R.layout.fragment_binder) {
    private val binding: FragmentBinderBinding by viewBinding()
    private val viewModel: BinderViewModel by viewModels { Injector.provideBinderViewModel(requireContext()) }
    private val launchBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted)
            viewModel.bluetoothPermissionsAreGranted()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().registerReceiver(viewModel.broadcastReceiver, BluetoothDevicesViewModel.REQUIRED_BROADCAST_FILTERS)

        binding.giveAccess.setOnClickListener {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        binding.turnOnBluetooth.setOnClickListener {
            launchBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        binding.rescan.setOnClickListener { viewModel.rescan() }

        viewModel.currentState.observeOnLifecycle(viewLifecycleOwner) { state ->
            updateLayoutVisibilityDependingOnState(state)
        }
    }

    private fun updateLayoutVisibilityDependingOnState(state: BinderViewModel.State) {
        binding.apply {
            layoutLoading.isVisible = state is BinderViewModel.State.Discovering
            layoutRequestPermissions.isVisible = state is BinderViewModel.State.PermissionDenied
            layoutTurnOnBluetooth.isVisible = state is BinderViewModel.State.BluetoothIsDisabled
            layoutRobotIsNotFound.isVisible = state is BinderViewModel.State.RobotNotFound
            layoutRobotIsFound.isVisible = state is BinderViewModel.State.RobotFound
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(viewModel.broadcastReceiver)
    }
}