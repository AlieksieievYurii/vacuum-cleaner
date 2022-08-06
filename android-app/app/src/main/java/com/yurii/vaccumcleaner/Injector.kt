package com.yurii.vaccumcleaner

import android.content.Context
import com.yurii.vaccumcleaner.robot.RobotSocketDiscovery

import com.yurii.vaccumcleaner.screens.loading.InitialFragmentViewModel

object Injector {

    fun provideInitialFragmentViewModel(context: Context): InitialFragmentViewModel.Factory {
        val robotSocketDiscovery = RobotSocketDiscovery(context)
        return InitialFragmentViewModel.Factory(context, robotSocketDiscovery)
    }
}