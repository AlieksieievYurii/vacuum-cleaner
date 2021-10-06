package com.yurii.vaccumcleaner.service

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.reflect.KClass

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
    @Json(name = "request_id") val requestId: Long,
    val parameters: P?
)

@JsonClass(generateAdapter = true)
data class Response<R : Any>(
    @Json(name = "request_id") val requestId: Long,
    @Json(name = "request_name") val requestName: String,
    val status: ResponseStatus,
    @Json(name = "error_message") val errorMessage: String?,
    val response: R?
)

abstract class RequestHandler<R : Any, P : Any>(private val requestName: String, responseModelClass: KClass<R>, parameters: KClass<P>?) {
    abstract fun handle(request: Request<P>): R
}