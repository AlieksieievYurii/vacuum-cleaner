package com.yurii.vaccumcleaner

import android.app.Activity
import com.yurii.vaccumcleaner.robot.RobotConnection
import com.yurii.vaccumcleaner.robot.RobotMockUpImpl
import com.yurii.vaccumcleaner.robot.RobotSocketDiscovery
import com.yurii.vaccumcleaner.screens.binder.BinderViewModel
import com.yurii.vaccumcleaner.screens.control.ManualControlViewModel

import com.yurii.vaccumcleaner.screens.loading.InitialFragmentViewModel
import com.yurii.vaccumcleaner.screens.panel.PanelViewModel

object Injector {

    fun provideBinderViewModel(): BinderViewModel.Factory {
        return BinderViewModel.Factory()
    }

    fun provideInitialFragmentViewModel(activity: Activity): InitialFragmentViewModel.Factory {
        val robotSocketDiscovery = RobotSocketDiscovery(activity)
        return InitialFragmentViewModel.Factory(
            activity,
            robotSocketDiscovery)
    }

    fun providePanelViewModel(): PanelViewModel.Factory {
        //return PanelViewModel.Factory(RobotConnection.getRobotAPI())
        return PanelViewModel.Factory(RobotMockUpImpl())
    }

    fun provideManualControlViewModel(): ManualControlViewModel.Factory {
        return ManualControlViewModel.Factory(RobotConnection.getRobotAPI())
    }
}