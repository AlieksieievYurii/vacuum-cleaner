package com.yurii.vaccumcleaner.requesthandler

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.yurii.vaccumcleaner.utils.pop
import com.yurii.vaccumcleaner.utils.synchronizedAppend
import kotlinx.coroutines.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread


class RequestHandler(private val communicator: Communicator) {
    private var moshi = Moshi.Builder().build()
    private val jsonAdapter = moshi.adapter<Request<*>>(Types.newParameterizedType(Request::class.java, Any::class.java))
    private val responseAdapter = moshi.adapter(Response::class.java)
    private val responses = mutableListOf<Response>()

    fun start() {
        thread(start = true) { listenForIncomingResponses() }
    }

    suspend fun <R : Any> send(endpoint: String, requestModel: Any?, responseModel: Class<R>?, timeout: Int = 1000): R? {
        val request = Request(endpoint = endpoint, requestId = UUID.randomUUID().toString(), parameters = requestModel)
        performRequest(request)
        return awaitForResponse(request, responseModel, timeout)
    }

    private suspend fun performRequest(request: Request<*>) = withContext(Dispatchers.IO) {
        val data = jsonAdapter.toJson(request)
        communicator.send(data)
    }

    private fun <R> awaitForResponse(request: Request<*>, responseClass: Class<R>?, timeout: Int): R? {
        val startTime = System.currentTimeMillis()
        while (true) {

            if (System.currentTimeMillis() - startTime > timeout)
                throw TimeoutException("No response from '${request.endpoint}'. Timeout: $timeout")

            val response = responses.pop(this) { it.requestId == request.requestId && it.endpoint == request.endpoint }
            response?.run {
                val data = if (this.data != null) JSONObject(this.data as Map<*, *>).toString() else "{}"
                when (this.status) {
                    ResponseStatus.OK -> return if (responseClass != null) moshi.adapter(responseClass).fromJson(data)!! else null
                    ResponseStatus.ERROR -> throw RequestFailed(request, this.errorMessage)
                    ResponseStatus.BAD_REQUEST -> throw BadRequest(request, this.errorMessage)
                }
            }
        }
    }

    private fun listenForIncomingResponses() {
        while (true) {
            val data = communicator.read()
            val d = responseAdapter.fromJson(data)
            Timber.d(data)
            responses.synchronizedAppend(this, d!!)
        }
    }
}