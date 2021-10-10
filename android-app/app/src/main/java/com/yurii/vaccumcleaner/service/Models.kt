package com.yurii.vaccumcleaner.service

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.json.JSONObject
import java.lang.Exception

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
        val packet = Packet(
            type = PacketType.RESPONSE,
            content = handleRequestAndGetResponse(moshi, request)
        )
        return moshi.createResponseModelAdapter(responseModelClass).toJson(packet)
    }

    private fun handleRequestAndGetResponse(moshi: Moshi, request: Request<*>): Response<R> = try {
        val output = if (parameters != null) handleRequestWithParameters(moshi, request) else handleNoParametrizedRequest(request)
        Response(
            requestId = request.requestId,
            requestName = requestName,
            status = ResponseStatus.OK,
            errorMessage = null,
            response = output
        )
    } catch (error: WrongParameters) {
        Response(
            requestId = request.requestId,
            requestName = requestName,
            status = ResponseStatus.BAD_REQUEST,
            errorMessage = error.message,
            response = null
        )
    }

    private fun handleNoParametrizedRequest(request: Request<*>): R {
        return handle(request, null)
    }

    private fun handleRequestWithParameters(moshi: Moshi, request: Request<*>): R {
        if (request.parameters == null)
            throw WrongParameters(request, "This request requires parameters!")

        val parameters = try {
            moshi.adapter(parameters!!).fromJson(JSONObject(request.parameters as Map<*, *>).toString())!!
        } catch (exception: Exception) {
            throw WrongParameters(request, "Wrong parameters")
        }
        return handle(request, parameters)
    }


    abstract fun handle(request: Request<*>, parameters: P?): R
}