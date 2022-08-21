package com.yurii.vaccumcleaner.utils.requesthandler

import java.lang.Exception
import java.util.concurrent.TimeoutException

class BadRequest(val request: Request<*>, val errorMessage: String?) : Exception() {
    override val message: String = "Request: ${request.endpoint} ID[${request.requestId}]. Message: $errorMessage"
}

class RequestTimeout : TimeoutException()

class RequestFailed(val request: Request<*>, val errorMessage: String?) : Exception() {
    override val message: String = "Request: ${request.endpoint} ID[${request.requestId}]. Message: $errorMessage"
}