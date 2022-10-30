package com.yurii.vaccumcleaner

import android.content.Context
import com.yurii.vaccumcleaner.robot.RobotBluetoothConnection
import com.yurii.vaccumcleaner.robot.RobotConnection
import com.yurii.vaccumcleaner.robot.RobotMockUpImpl
import com.yurii.vaccumcleaner.robot.RobotSocketDiscovery
import com.yurii.vaccumcleaner.screens.algo.AlgorithmSetupViewModel
import com.yurii.vaccumcleaner.screens.binder.BinderViewModel
import com.yurii.vaccumcleaner.screens.control.ManualControlViewModel
import com.yurii.vaccumcleaner.screens.execution.CleaningExecutionViewModel

import com.yurii.vaccumcleaner.screens.loading.InitialFragmentViewModel
import com.yurii.vaccumcleaner.screens.panel.PanelViewModel
import com.yurii.vaccumcleaner.screens.pid.PidSettingsViewModel
import com.yurii.vaccumcleaner.screens.wifi.WifiSettingsViewModel
import com.yurii.vaccumcleaner.utils.ServiceContextManager

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
        val apiProvider = if (bluetoothProvider) RobotBluetoothConnection.getRobotAPI() else RobotConnection.getRobotAPI()
        return WifiSettingsViewModel.Factory(apiProvider)
    }

    fun providePanelViewModel(): PanelViewModel.Factory {
        return PanelViewModel.Factory(RobotConnection.getRobotAPI())
        //return PanelViewModel.Factory(RobotMockUpImpl())
    }

    fun providePidSettingsViewModel(): PidSettingsViewModel.Factory {
        return PidSettingsViewModel.Factory(RobotConnection.getRobotAPI())
    }

    fun provideCleaningExecutionViewModel(): CleaningExecutionViewModel.Factory {
        return CleaningExecutionViewModel.Factory(RobotConnection.getRobotAPI())
    }

    fun provideManualControlViewModel(): ManualControlViewModel.Factory {
        return ManualControlViewModel.Factory(RobotConnection.getRobotAPI())
        //return ManualControlViewModel.Factory(RobotMockUpImpl())
    }

    fun provideAlgorithmSetupViewModel(): AlgorithmSetupViewModel.Factory {
        return AlgorithmSetupViewModel.Factory(RobotConnection.getRobotAPI())
    }
}