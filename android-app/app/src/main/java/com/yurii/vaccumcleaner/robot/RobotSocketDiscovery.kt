package com.yurii.vaccumcleaner.robot

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import com.yurii.vaccumcleaner.reverseBytes
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

class RobotSocketDiscovery(context: Context) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    suspend fun discover(scope: CoroutineScope): List<String> {
        var k = 0
        val ips = getAllAvailableIPs().toMutableList()
        val bufferIps = mutableListOf<String>()
        val listOfJobs = mutableListOf<Deferred<List<String>>>()

        while (true) {
            val ip = ips.removeFirst()

            if (k == 5) {
                val newBufferedList = bufferIps.toList()
                listOfJobs.add(scope.async { scanIps(newBufferedList) })
                bufferIps.clear()
                k = 0
            }else {
                bufferIps.add(ip)
                k++
            }

            if (ips.isEmpty()) {
                val newBufferedList = bufferIps.toList()
                listOfJobs.add(scope.async { scanIps(newBufferedList) })
                break
            }
        }

        val res = mutableListOf<String>()
        listOfJobs.awaitAll().forEach { it.forEach { ips -> res.add(ips) } }
        return res
    }

    private suspend fun scanIps(ips: List<String>): List<String> {
        val res = mutableListOf<String>()
        ips.forEach { ip ->
            if (tryConnect(ip))
                res.add(ip)
        }
        return res
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun tryConnect(ip: String) = withContext(Dispatchers.IO) {
        val soc = Socket()
        try {
            soc.connect(InetSocketAddress(ip, 1488), 1000)
            return@withContext true
        } catch (timeout: SocketTimeoutException) {
            Timber.d("Attempt failure(Timeout) to connect to Vacuum Robot by: $ip")
        } catch (connection: ConnectException) {
            Timber.d("Attempt failure(Connection refuse) to connect to Vacuum Robot by: $ip")
        }

        return@withContext false
    }

    private fun sendKey(socket: Socket) {

    }

    private fun getAllAvailableIPs(): List<String> {
        val reversedBitsIp = wifiManager.dhcpInfo.gateway.reverseBytes()

        return (1 until getRangeNumberOfSubnetMask(wifiManager.dhcpInfo.netmask)).map {
            Formatter.formatIpAddress((reversedBitsIp + it).reverseBytes())
        }
    }

    private fun getRangeNumberOfSubnetMask(subnetMask: Int): Int {
        (0 until 32).forEach { bitIndex ->
            if ((subnetMask and (1 shl bitIndex)) == 0) {
                var sumOfBits = 0
                (0 until 32 - bitIndex).forEach {
                    sumOfBits = sumOfBits or (1 shl it)
                }
                return sumOfBits
            }
        }
        return Int.MAX_VALUE
    }
}