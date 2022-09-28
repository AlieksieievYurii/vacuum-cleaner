package com.yurii.vaccumcleaner

import android.content.Context
import com.yurii.vaccumcleaner.robot.RobotConnection
import com.yurii.vaccumcleaner.robot.RobotMockUpImpl
import com.yurii.vaccumcleaner.robot.RobotSocketDiscovery
import com.yurii.vaccumcleaner.screens.binder.BinderViewModel
import com.yurii.vaccumcleaner.screens.control.ManualControlViewModel

import com.yurii.vaccumcleaner.screens.loading.InitialFragmentViewModel
import com.yurii.vaccumcleaner.screens.panel.PanelViewModel
import com.yurii.vaccumcleaner.screens.pid.PidSettingsViewModel

object Injector {

    fun provideBinderViewModel(): BinderViewModel.Factory {
        return BinderViewModel.Factory()
    }

    fun provideInitialFragmentViewModel(context: Context): InitialFragmentViewModel.Factory {
        return InitialFragmentViewModel.Factory(
            Preferences(context),
            RobotSocketDiscovery(context)
        )
    }

    fun providePanelViewModel(): PanelViewModel.Factory {
        //return PanelViewModel.Factory(RobotConnection.getRobotAPI())
        return PanelViewModel.Factory(RobotMockUpImpl())
    }

    fun providePidSettingsViewModel(): PidSettingsViewModel.Factory {
        return PidSettingsViewModel.Factory(RobotMockUpImpl())
    }

    fun provideManualControlViewModel(): ManualControlViewModel.Factory {
        return ManualControlViewModel.Factory(RobotConnection.getRobotAPI())
    }
}