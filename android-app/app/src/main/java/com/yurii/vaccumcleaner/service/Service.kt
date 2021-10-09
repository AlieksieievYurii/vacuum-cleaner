package com.yurii.vaccumcleaner.service

import android.bluetooth.BluetoothDevice
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.concurrent.TimeoutException

fun <R : Any> Moshi.createResponseModelAdapter(responseClass: Class<R>): JsonAdapter<Packet<Response<R>>> =
    this.adapter(Types.newParameterizedType(Packet::class.java, Types.newParameterizedType(Response::class.java, responseClass)))

fun <R : Any> Moshi.createParametrizedRequestAdapter(parameters: Class<R>): JsonAdapter<Packet<Request<R>>> =
    this.adapter(Types.newParameterizedType(Packet::class.java, Types.newParameterizedType(Request::class.java, parameters)))

class Service(
    private val coroutineScope: CoroutineScope,
    bluetoothDevice: BluetoothDevice,
    private val requestHandlers: List<RequestHandler<*, *>> = emptyList()
) {
    private val communicator = BluetoothCommunicator(bluetoothDevice)

    private val moshi = Moshi.Builder().build()

    private val packetAdapter = moshi.adapter<Packet<*>>(Types.newParameterizedType(Packet::class.java, Any::class.java))
    private val packetResponseAdapter = moshi.adapter<Packet<Response<Any>>>(
        Types.newParameterizedType(Packet::class.java, Types.newParameterizedType(Response::class.java, Any::class.java))
    )

    private val packetRequestAdapter = moshi.adapter<Packet<Request<*>>>(
        Types.newParameterizedType(Packet::class.java, Types.newParameterizedType(Request::class.java, Any::class.java))
    )
    private val noParametrizedRequestAdapter = moshi.adapter<Packet<Request<*>>>(
        Types.newParameterizedType(Packet::class.java, Types.newParameterizedType(Request::class.java, Any::class.java))
    )

    private val _receivedPackets: MutableSharedFlow<Packet<*>> = MutableSharedFlow()
    val receivedPackets = _receivedPackets.asSharedFlow()

    private val _brokenPacket: MutableSharedFlow<Pair<String, Exception>> = MutableSharedFlow()
    val brokenPackets = _brokenPacket.asSharedFlow()

    suspend fun start() {
        communicator.connect()
        coroutineScope.launch {
            communicator.startListening()
        }
        coroutineScope.launch {
            communicator.output.collect { packetJson ->
                try {
                    val packet = parsePacket(packetJson)
                    _receivedPackets.emit(packet)
                    handleIfRequest(packet)
                } catch (error: Exception) {
                    _brokenPacket.emit(packetJson to error)
                }
            }
        }
    }

    private fun handleIfRequest(packet: Packet<*>) {
        if (packet.type == PacketType.RESPONSE)
            return
        val request = packet.content as Request<*>
        val requestHandler = requestHandlers.find { it.requestName == request.requestName }
            ?: throw IllegalStateException("Cannot find request handler for ${request.requestName}")
        handleRequest(request, requestHandler)
    }

    private fun handleRequest(request: Request<*>, requestHandler: RequestHandler<*, *>) {
        coroutineScope.launch(Dispatchers.IO) {
            val response = requestHandler.handleIncomingRequest(moshi, request)
            communicator.send(response)
        }
    }

    private fun parsePacket(json: String): Packet<*> {
        val packet = packetAdapter.fromJson(json)!!
        return when (packet.type) {
            PacketType.REQUEST -> packetRequestAdapter.fromJson(json)!!
            PacketType.RESPONSE -> packetResponseAdapter.fromJson(json)!!
        }
    }

    suspend fun <R : Any, P : Any> request(
        requestName: String,
        parameters: P,
        responseClass: Class<R>,
        parametersClass: Class<P>,
        timeout: Long = 10000L
    ): R = withContext(Dispatchers.IO) {
        val request = Request(
            requestName = requestName,
            requestId = System.currentTimeMillis(),
            parameters = parameters
        )

        communicator.send(moshi.createParametrizedRequestAdapter(parametersClass).toJson(Packet(PacketType.REQUEST, request)))
        return@withContext withTimeoutOrNull(timeout) { awaitForResponse(request, responseClass) }
            ?: throw TimeoutException("No response from $requestName. Parameters: $parameters")
    }

    suspend fun <R : Any> request(requestName: String, responseClass: Class<R>, timeout: Long = 10000L): R = withContext(Dispatchers.IO) {
        val request = Request(
            requestName = requestName,
            requestId = System.currentTimeMillis(),
            parameters = null
        )

        communicator.send(noParametrizedRequestAdapter.toJson(Packet(PacketType.REQUEST, request)))
        return@withContext withTimeoutOrNull(timeout) { awaitForResponse(request, responseClass) }
            ?: throw TimeoutException("No response from $requestName")
    }

    private suspend fun <R : Any> awaitForResponse(request: Request<*>, responseClass: Class<R>): R {
        while (true)
            return getResponseFromRequest(request, communicator.output.first(), responseClass) ?: continue
    }

    private fun <R : Any> getResponseFromRequest(request: Request<*>, jsonResponse: String, responseClass: Class<R>): R? {
        val packet = try {
            packetAdapter.fromJson(jsonResponse)!!
        }catch (e: Exception) {
            return null
        }

        if (packet.type == PacketType.REQUEST)
            return null

        val responsePacket = packetResponseAdapter.fromJson(jsonResponse)!!.content

        if (responsePacket.requestId != request.requestId || responsePacket.requestName != request.requestName)
            return null

        val packetRespAdapter = moshi.createResponseModelAdapter(responseClass)
        return packetRespAdapter.fromJson(jsonResponse)!!.content.response
    }

}