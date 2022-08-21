package com.yurii.vaccumcleaner.robot

import com.yurii.vaccumcleaner.utils.requesthandler.RequestHandler

object RobotConnection {
    private val wifiCommunicator = WifiCommunicator()
    private val requestHandler = RequestHandler(wifiCommunicator)
    private val robot = Robot(requestHandler)

    suspend fun makeConnection(ip: String, port: Int) {
        wifiCommunicator.connect(ip, port)
        requestHandler.start()
    }

    fun getRobotAPI() = robot
}