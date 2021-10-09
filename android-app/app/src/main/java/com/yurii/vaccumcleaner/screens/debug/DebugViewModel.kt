package com.yurii.vaccumcleaner.screens.debug

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.yurii.vaccumcleaner.addUnique
import com.yurii.vaccumcleaner.service.*
import com.yurii.vaccumcleaner.screens.debug.Packet as RVPacket
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class BluetoothStatus {
    DISCONNECTED, CONNECTING, CONNECTED
}

@JsonClass(generateAdapter = true)
data class CheckParameters(
    @Json(name = "server_name") val serverName: String
)

@JsonClass(generateAdapter = true)
data class CheckResponseModel(
    @Json(name = "server_name") val serverName: String,
    @Json(name = "client_name") val clientName: String
)

class CheckRequestHandler :
    RequestHandler<CheckResponseModel, CheckParameters>("validation_request", CheckResponseModel::class.java, CheckParameters::class.java) {
    override fun handle(request: Request<*>, parameters: CheckParameters?): CheckResponseModel {
        return CheckResponseModel(
            serverName = parameters!!.serverName,
            clientName = "Yurii"
        )
    }
}

class DebugViewModel(bluetoothDevice: BluetoothDevice) : ViewModel() {
    private val service = Service(viewModelScope, bluetoothDevice, requestHandlers = listOf(CheckRequestHandler()))
    private val _bluetoothStatus: MutableStateFlow<BluetoothStatus> = MutableStateFlow(BluetoothStatus.DISCONNECTED)
    val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus

    private val coroutineException = CoroutineExceptionHandler { _, exception ->
        _bluetoothStatus.value = BluetoothStatus.DISCONNECTED
    }

    private val _packets: MutableStateFlow<List<RVPacket>> = MutableStateFlow(emptyList())
    val packets: StateFlow<List<RVPacket>> = _packets

    init {
        connectBluetooth()
    }

    fun connectBluetooth() {
        viewModelScope.launch(coroutineException) {
            _bluetoothStatus.value = BluetoothStatus.CONNECTING
            service.start()
            _bluetoothStatus.value = BluetoothStatus.CONNECTED
            startListeningToAllOutput()
        }
    }

    private fun startListeningToAllOutput() {
        viewModelScope.launch {
            service.receivedPackets.collect {
                val packet = convertToRVPacket(it)
                _packets.addUnique(packet)
            }
        }
        viewModelScope.launch {
            service.brokenPackets.collect {
                _packets.addUnique(RVPacket.Broken(content = it.first, error = it.second))
            }
        }
    }

    private fun convertToRVPacket(packet: Packet<*>): RVPacket = when (packet.type) {
        PacketType.REQUEST -> {
            val request = packet.content as Request<*>
            RVPacket.Request(
                requestName = request.requestName,
                requestId = request.requestId,
                parameters = JSONObject(request.parameters as Map<*, *>).toString(),
                isSent = false
            )
        }

        PacketType.RESPONSE -> {
            val response = packet.content as Response<*>
            RVPacket.Response(
                requestName = response.requestName,
                requestId = response.requestId,
                response = JSONObject(response.response as Map<*, *>).toString(),
                isSent = false,
                status = response.status.name,
                errorMessage = response.errorMessage
            )
        }
    }

    class Factory(private val bluetoothDevice: BluetoothDevice) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DebugViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DebugViewModel(bluetoothDevice) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}