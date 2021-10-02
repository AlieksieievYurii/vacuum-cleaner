package com.yurii.vaccumcleaner.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.util.*


class BluetoothCommunicator(private val bluetoothDevice: BluetoothDevice) {
    private val socket: BluetoothSocket by lazy {
        bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"))
    }

    private val _output = MutableSharedFlow<String>(replay = 5)
    val output: SharedFlow<String> = _output

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun connect() = withContext(Dispatchers.IO) {
        socket.connect()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun startListening() = withContext(Dispatchers.IO) {
        val mmInStream = socket.inputStream
        while (true) {
            val available = mmInStream.available()
            if (available > 0) {
                val mmBuffer = ByteArray(available)
                try {
                    mmInStream.read(mmBuffer)
                    _output.emit(String(mmBuffer, Charsets.UTF_8))
                } catch (e: IOException) {
                    break
                }
            }
        }
    }


    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun send(text: String) = withContext(Dispatchers.IO) {
        Timber.d("Data to send via Bluetooth: $text")
        socket.outputStream.write(text.toByteArray())
    }

}