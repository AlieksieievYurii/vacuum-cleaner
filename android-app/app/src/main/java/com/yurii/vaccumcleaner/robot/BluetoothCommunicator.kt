package com.yurii.vaccumcleaner.robot

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.yurii.vaccumcleaner.utils.requesthandler.Communicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.*

class BluetoothCommunicator : Communicator {

    private var bluetoothSocket: BluetoothSocket? = null
    private var socketReader: BufferedReader? = null

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun makeConnection(device: BluetoothDevice) = withContext(Dispatchers.IO) {
        bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(ROBOT_UUID)).apply {
            connect()
        }
        socketReader = BufferedReader(InputStreamReader(bluetoothSocket!!.inputStream))
    }

    fun isConnected() = bluetoothSocket?.isConnected ?: false

    override fun read(): String {
        return socketReader!!.readLine()
    }

    override fun send(data: String) {
        val writer = PrintWriter(bluetoothSocket!!.outputStream, true)
        writer.println(data)
    }

    companion object {
        private const val ROBOT_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
    }
}