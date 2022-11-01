package com.yurii.vaccumcleaner.robot

import com.yurii.vaccumcleaner.utils.requesthandler.Communicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.IllegalStateException
import java.net.InetSocketAddress
import java.net.Socket

class WifiCommunicator : Communicator {
    private var socket: Socket? = null
    private var socketReader: BufferedReader? = null

    suspend fun connect(ip: String, port: Int) = withContext(Dispatchers.IO) {
        socket = Socket().apply {
            connect(InetSocketAddress(ip, port), 4000)
        }
        socketReader = BufferedReader(InputStreamReader(socket!!.inputStream))
    }

    fun isConnected() = socket?.isConnected ?: false

    override fun read(): String {
        return socketReader?.readLine() ?: throw IllegalStateException("Can not read from the socket because it is null. You need to connect first")
    }

    override fun send(data: String) {
        val outStream = socket?.outputStream ?: throw IllegalStateException("Cant print to the socket because it is null. You need to connect first")
        val writer = PrintWriter(outStream, true)
        writer.println(data)
    }

    override fun close() {
        socket?.close()
    }

}