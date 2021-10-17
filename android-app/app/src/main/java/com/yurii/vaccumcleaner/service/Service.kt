package com.yurii.vaccumcleaner.service

import android.bluetooth.BluetoothDevice
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.*
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

    private val responses = mutableListOf<Response<*>>()

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
                    when (packet.type) {
                        PacketType.REQUEST -> handleRequest(packet.content as Request<*>)
                        PacketType.RESPONSE -> addToResponseRow(packet.content as Response<*>)
                    }
                } catch (error: Exception) {
                    _brokenPacket.emit(packetJson to error)
                }
            }
        }
    }

    private fun addToResponseRow(response: Response<*>) {
        if (responses.size > 5)
            responses.removeFirst()
        responses.add(response)
    }

    private fun handleRequest(request: Request<*>) {
        val requestHandler = requestHandlers.find { it.requestName == request.requestName }
            ?: throw IllegalStateException("Cannot find request handler for ${request.requestName}")

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
            requestId = UUID.randomUUID().toString(),
            parameters = parameters
        )

        communicator.send(moshi.createParametrizedRequestAdapter(parametersClass).toJson(Packet(PacketType.REQUEST, request)))
        return@withContext awaitForResponse(request, responseClass, timeout)
    }

    suspend fun <R : Any> request(requestName: String, responseClass: Class<R>, timeout: Long = 10000L): R = withContext(Dispatchers.IO) {
        val request = Request(
            requestName = requestName,
            requestId = UUID.randomUUID().toString(),
            parameters = null
        )

        communicator.send(noParametrizedRequestAdapter.toJson(Packet(PacketType.REQUEST, request)))
        return@withContext awaitForResponse(request, responseClass, timeout)
    }

    private fun <R : Any> awaitForResponse(request: Request<*>, responseClass: Class<R>, timeout: Long = 10000L): R {
        val startTime = System.currentTimeMillis()
        while (true) {
            if (System.currentTimeMillis() - startTime > timeout)
                throw TimeoutException("No response from ${request.requestName}. Timeout: $timeout")

            synchronized(this) {
                responses.find { it.requestId == request.requestId && it.requestName == request.requestName }?.run {
                    responses.remove(this)
                    if (this.status != ResponseStatus.OK)
                        throw IllegalStateException(this.errorMessage)

                    return moshi.adapter(responseClass).fromJson(JSONObject(this.response as Map<*, *>).toString())!!
                }
            }
        }
    }
}