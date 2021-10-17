package com.yurii.vaccumcleaner.service

import java.lang.Exception

class WrongParameters(val request: Request<*>, val errorMessage: String) : Exception() {
    override val message: String = "Request: ${request.requestName} ID[${request.requestId}]. Message: $errorMessage"
}
