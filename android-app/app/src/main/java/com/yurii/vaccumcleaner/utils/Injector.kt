package com.yurii.vaccumcleaner.utils

import android.content.Context
import com.yurii.vaccumcleaner.robot.RobotBluetoothConnection
import com.yurii.vaccumcleaner.robot.RobotWifiConnection
import com.yurii.vaccumcleaner.robot.RobotSocketDiscovery
import com.yurii.vaccumcleaner.screens.algo.AlgorithmSetupViewModel
import com.yurii.vaccumcleaner.screens.binder.BinderViewModel
import com.yurii.vaccumcleaner.screens.control.ManualControlViewModel
import com.yurii.vaccumcleaner.screens.execution.CleaningExecutionViewModel

import com.yurii.vaccumcleaner.screens.loading.InitialFragmentViewModel
import com.yurii.vaccumcleaner.screens.panel.PanelViewModel
import com.yurii.vaccumcleaner.screens.pid.PidSettingsViewModel
import com.yurii.vaccumcleaner.screens.wifi.WifiSettingsViewModel

object Injector {

    fun provideBinderViewModel(context: Context): BinderViewModel.Factory {
        return BinderViewModel.Factory(context)
    }

    fun provideInitialFragmentViewModel(context: Context): InitialFragmentViewModel.Factory {
        return InitialFragmentViewModel.Factory(
            ServiceContextManager(context),
            Preferences(context),
            RobotSocketDiscovery(context)
        )
    }

    fun provideWifiSettingsViewModel(bluetoothProvider: Boolean): WifiSettingsViewModel.Factory {
        val apiProvider = if (bluetoothProvider) RobotBluetoothConnection.getRobotAPI() else RobotWifiConnection.getRobotAPI()
        return WifiSettingsViewModel.Factory(apiProvider)
    }

    fun providePanelViewModel(): PanelViewModel.Factory {
        return PanelViewModel.Factory(RobotWifiConnection.getRobotAPI())
        //return PanelViewModel.Factory(RobotMockUpImpl())
    }

    fun providePidSettingsViewModel(): PidSettingsViewModel.Factory {
        return PidSettingsViewModel.Factory(RobotWifiConnection.getRobotAPI())
    }

    fun provideCleaningExecutionViewModel(): CleaningExecutionViewModel.Factory {
        return CleaningExecutionViewModel.Factory(RobotWifiConnection.getRobotAPI())
    }

    fun provideManualControlViewModel(): ManualControlViewModel.Factory {
        return ManualControlViewModel.Factory(RobotWifiConnection.getRobotAPI())
        //return ManualControlViewModel.Factory(RobotMockUpImpl())
    }

    fun provideAlgorithmSetupViewModel(): AlgorithmSetupViewModel.Factory {
        return AlgorithmSetupViewModel.Factory(RobotWifiConnection.getRobotAPI())
    }
}