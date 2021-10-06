package com.yurii.vaccumcleaner.service

import android.bluetooth.BluetoothDevice
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.concurrent.TimeoutException

fun <R : Any> Moshi.createResponseModelAdapter(responseClass: Class<R>): JsonAdapter<Packet<Response<R>>> =
    this.adapter(Types.newParameterizedType(Packet::class.java, Types.newParameterizedType(Response::class.java, responseClass)))

fun <R : Any> Moshi.createParametrizedRequestAdapter(parameters: Class<R>): JsonAdapter<Request<R>> =
    this.adapter(Types.newParameterizedType(Request::class.java, parameters))

class Service(private val coroutineScope: CoroutineScope, bluetoothDevice: BluetoothDevice, private val requestHandlers: List<RequestHandler<*, *>>) {
    private val communicator = BluetoothCommunicator(bluetoothDevice)

    private val moshi = Moshi.Builder().build()

    private val packetAdapter = moshi.adapter<Packet<Any>>(Types.newParameterizedType(Packet::class.java, Any::class.java))
    private val packetModelAdapter = moshi.adapter<Packet<Response<Any>>>(
        Types.newParameterizedType(Packet::class.java, Types.newParameterizedType(Response::class.java, Any::class.java))
    )
    private val noParametrizedRequestAdapter = moshi.adapter<Packet<Request<Any>>>(
        Types.newParameterizedType(Packet::class.java, Types.newParameterizedType(Request::class.java, Any::class.java))
    )

    suspend fun start() {
        communicator.connect()
        coroutineScope.launch {
            communicator.startListening()
        }
        coroutineScope.launch {
            communicator.output.collect {
                Timber.d("Data received: $it")
            }
        }
    }

//    private fun parseAsRequest(json: String): Request<*> {
//        val packet = packetAdapter.fromJson(jsonResponse)!!
////        if (packet.type == PacketType.REQUEST)
////            return null
//    }

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

        communicator.send(moshi.createParametrizedRequestAdapter(parametersClass).toJson(request))
        return@withContext withTimeoutOrNull(timeout) { awaitForResponse(request, responseClass) }
            ?: throw TimeoutException("No response from $requestName. Parameters: $parameters")
    }

    suspend fun <R : Any> request(requestName: String, responseClass: Class<R>, timeout: Long = 10000L): R = withContext(Dispatchers.IO) {
        val request = Request<Any>(
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
        val packet = packetAdapter.fromJson(jsonResponse)!!
        if (packet.type == PacketType.REQUEST)
            return null

        val r = packetModelAdapter.fromJson(jsonResponse)!!.content

        if (r.requestId != request.requestId || r.requestName != request.requestName)
            return null

        val packetRespAdapter = moshi.createResponseModelAdapter(responseClass)
        return packetRespAdapter.fromJson(jsonResponse)!!.content.response
    }

}