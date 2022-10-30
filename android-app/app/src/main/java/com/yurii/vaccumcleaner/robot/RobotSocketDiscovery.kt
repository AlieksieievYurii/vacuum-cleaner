package com.yurii.vaccumcleaner.robot

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import com.yurii.vaccumcleaner.MyApplication
import com.yurii.vaccumcleaner.utils.reverseBytes
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.IllegalStateException
import java.net.*

class RobotSocketDiscovery(context: Context) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    suspend fun discover(scope: CoroutineScope): List<String> {
        var k = 0
        val ips = getAllAvailableIPs().toMutableList()
        val bufferIps = mutableListOf<String>()
        val listOfJobs = mutableListOf<Deferred<List<String>>>()
        val res = mutableListOf<String>()

        if (ips.isEmpty())
            return res

        while (true) {
            val ip = ips.removeFirst()

            if (k == 5) {
                val newBufferedList = bufferIps.toList()
                listOfJobs.add(scope.async { scanIps(newBufferedList) })
                bufferIps.clear()
                k = 0
            } else {
                bufferIps.add(ip)
                k++
            }

            if (ips.isEmpty()) {
                val newBufferedList = bufferIps.toList()
                listOfJobs.add(scope.async { scanIps(newBufferedList) })
                break
            }
        }


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
            soc.connect(InetSocketAddress(ip, MyApplication.ROBOT_SOCKET_PORT), 1000)
            soc.close()
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

        return (1 until getHostsPerSubnet(wifiManager.dhcpInfo.netmask)).map {
            Formatter.formatIpAddress((reversedBitsIp + it).reverseBytes())
        }
    }

    private fun getHostsPerSubnet(subnetMask: Int): Int =
        when(val it = Formatter.formatIpAddress(subnetMask)) {
            "255.0.0.0" -> 16_277_214
            "255.128.0.0" -> 8_388_606
            "255.192.0.0" -> 4_194_302
            "255.224.0.0" -> 2_097_150
            "255.240.0.0" -> 1_048_574
            "255.248.0.0" -> 524_286
            "255.252.0.0" -> 262_142
            "255.254.0.0" -> 131_070
            "255.255.0.0" -> 65_534
            "255.255.128.0" -> 32_766
            "255.255.192.0" -> 16_382
            "255.255.224.0" -> 8_190
            "255.255.240.0" -> 4_094
            "255.255.248.0" -> 2_046
            "255.255.252.0" -> 1_022
            "255.255.254.0" -> 510
            "255.255.255.0" -> 254
            "255.255.255.128" -> 126
            "255.255.255.192" -> 62
            "255.255.255.224" -> 30
            "255.255.255.240" -> 14
            "255.255.255.248" -> 6
            "255.255.255.252" -> 2
            else -> throw IllegalStateException("Unhandled network mask! $it")
    }
}