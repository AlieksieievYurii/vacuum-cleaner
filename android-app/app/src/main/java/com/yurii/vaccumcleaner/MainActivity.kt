package com.yurii.vaccumcleaner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.yurii.vaccumcleaner.robot.RobotBluetoothConnection
import com.yurii.vaccumcleaner.robot.RobotWifiConnection

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel
    }

    override fun onDestroy() {
        super.onDestroy()
        RobotWifiConnection.closeConnectionIfOpen()
        RobotBluetoothConnection.closeConnectionIfOpened()
    }
}