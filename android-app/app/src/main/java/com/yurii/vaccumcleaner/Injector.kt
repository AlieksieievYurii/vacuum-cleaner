package com.yurii.vaccumcleaner

import android.content.Context
import com.yurii.vaccumcleaner.robot.RobotSocketDiscovery
import com.yurii.vaccumcleaner.screens.binder.BinderViewModel
import com.yurii.vaccumcleaner.screens.control.ManualControlViewModel

import com.yurii.vaccumcleaner.screens.loading.InitialFragmentViewModel
import com.yurii.vaccumcleaner.screens.panel.PanelViewModel

object Injector {

    fun provideBinderViewModel(): BinderViewModel.Factory {
        return BinderViewModel.Factory()
    }

    fun provideInitialFragmentViewModel(context: Context): InitialFragmentViewModel.Factory {
        val robotSocketDiscovery = RobotSocketDiscovery(context)
        return InitialFragmentViewModel.Factory(context, robotSocketDiscovery)
    }

    fun providePanelViewModel(): PanelViewModel.Factory {
        return PanelViewModel.Factory()
    }

    fun provideManualControlViewModel(): ManualControlViewModel.Factory {
        return ManualControlViewModel.Factory()
    }
}