package com.yurii.vaccumcleaner.robot

import android.bluetooth.BluetoothDevice
import com.yurii.vaccumcleaner.utils.requesthandler.RequestHandler
import java.lang.IllegalArgumentException

object RobotConnection {
    private val wifiCommunicator = WifiCommunicator()
    private val requestHandler = RequestHandler(wifiCommunicator)
    private val robot = RobotWifiImplementation(requestHandler)

    suspend fun makeConnection(ip: String, port: Int) {
        wifiCommunicator.connect(ip, port)
        requestHandler.start()
    }

    fun closeConnectionIfOpen() {
        if (wifiCommunicator.isConnected())
            wifiCommunicator.closeConnection()
    }

    fun getRobotAPI() = robot
}


object RobotBluetoothConnection {
    private val bluetoothCommunicator = BluetoothCommunicator()
    private val requestHandler = RequestHandler(bluetoothCommunicator)
    private val robot = RobotWifiImplementation(requestHandler)

    suspend fun makeConnection(bluetoothDevice: BluetoothDevice) {
        bluetoothCommunicator.makeConnection(bluetoothDevice)
        requestHandler.start()
    }

    fun getRobotAPI(): Robot {
        if (bluetoothCommunicator.isConnected())
            return robot
        throw IllegalArgumentException("No connection! You may call makeConnection first")
    }
}