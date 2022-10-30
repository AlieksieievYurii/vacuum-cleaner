package com.yurii.vaccumcleaner.robot

import com.yurii.vaccumcleaner.utils.requesthandler.Communicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class WifiCommunicator : Communicator {
    private lateinit var socket: Socket
    private lateinit var socketReader: BufferedReader

    suspend fun connect(ip: String, port: Int) = withContext(Dispatchers.IO) {
        socket = Socket()
        socket.connect(InetSocketAddress(ip, port), 4000)
        socketReader = BufferedReader(InputStreamReader(socket.inputStream))
    }

    fun isConnected() = socket.isConnected

    override fun read(): String {
        return socketReader.readLine()
    }

    override fun send(data: String) {
        val writer = PrintWriter(socket.outputStream, true)
        writer.println(data)
    }

    override fun close() {
        socket.close()
    }

}