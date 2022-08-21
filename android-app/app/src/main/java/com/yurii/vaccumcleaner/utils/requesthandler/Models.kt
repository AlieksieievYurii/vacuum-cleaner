package com.yurii.vaccumcleaner.utils.requesthandler

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class ResponseStatus {
    @Json(name = "OK")
    OK,

    @Json(name = "ERROR")
    ERROR,

    @Json(name = "BAD_REQUEST")
    BAD_REQUEST
}

@JsonClass(generateAdapter = true)
data class Request<P>(
    val endpoint: String,
    @Json(name = "request_id") val requestId: String,
    val parameters: P?
)

@JsonClass(generateAdapter = true)
data class Response(
    val endpoint: String,
    @Json(name = "request_id") val requestId: String,
    val status: ResponseStatus,
    val data: Any?,
    @Json(name = "error_message") val errorMessage: String?
)