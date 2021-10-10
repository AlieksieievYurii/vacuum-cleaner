package com.yurii.vaccumcleaner.service

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.json.JSONObject

@JsonClass(generateAdapter = false)
enum class ResponseStatus {
    @Json(name = "OK")
    OK,

    @Json(name = "ERROR")
    ERROR,

    @Json(name = "BAD_REQUEST")
    BAD_REQUEST
}

@JsonClass(generateAdapter = false)
enum class PacketType {
    @Json(name = "REQUEST")
    REQUEST,

    @Json(name = "RESPONSE")
    RESPONSE
}

@JsonClass(generateAdapter = true)
data class Packet<C>(
    val type: PacketType,
    val content: C
)

@JsonClass(generateAdapter = true)
data class Request<P>(
    @Json(name = "request_name") val requestName: String,
    @Json(name = "request_id") val requestId: String,
    val parameters: P?
)

@JsonClass(generateAdapter = true)
data class Response<R : Any>(
    @Json(name = "request_id") val requestId: String,
    @Json(name = "request_name") val requestName: String,
    val status: ResponseStatus,
    @Json(name = "error_message") val errorMessage: String?,
    val response: R?
)

abstract class RequestHandler<R : Any, P : Any>(
    val requestName: String,
    private val responseModelClass: Class<R>,
    private val parameters: Class<P>?
) {
    fun handleIncomingRequest(moshi: Moshi, request: Request<*>): String {
        val inputParameters: P? =
            if (parameters != null) moshi.adapter(parameters).fromJson(JSONObject(request.parameters as Map<*, *>).toString())!! else null
        val output = handle(request, inputParameters)
        val response = Response(
            requestId = request.requestId,
            requestName = requestName,
            status = ResponseStatus.OK,
            errorMessage = null,
            response = output
        )
        val packet = Packet(
            type = PacketType.RESPONSE,
            content = response
        )
        return moshi.createResponseModelAdapter(responseModelClass).toJson(packet)
    }

    abstract fun handle(request: Request<*>, parameters: P?): R
}