package com.yurii.vaccumcleaner.requesthandler

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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
    val data: Any
)