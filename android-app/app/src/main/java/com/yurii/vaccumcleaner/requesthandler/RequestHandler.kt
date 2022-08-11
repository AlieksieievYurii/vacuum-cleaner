package com.yurii.vaccumcleaner.requesthandler

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.yurii.vaccumcleaner.pop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeoutException


class RequestHandler(private val communicator: Communicator, private val scope: CoroutineScope) {
    private var moshi = Moshi.Builder().build()
    private val jsonAdapter = moshi.adapter<Request<*>>(Types.newParameterizedType(Request::class.java, Any::class.java))
    private val responseAdapter = moshi.adapter(Response::class.java)
    private val responses = mutableListOf<Response>()

    fun start() {
        scope.launch(Dispatchers.IO) {
            listenForIncomingResponses()
        }
    }

    suspend fun <R : Any> send(endpoint: String, requestModel: Any, responseModel: Class<R>): R {
        val request = Request(endpoint = endpoint, requestId = "2", parameters = requestModel)
        performRequest(request)
        return awaitForResponse(request, responseModel, 1000)
    }

    private suspend fun performRequest(request: Request<*>) = withContext(Dispatchers.IO) {
        val data = jsonAdapter.toJson(request)
        communicator.send(data)
    }

    private fun <R> awaitForResponse(request: Request<*>, responseClass: Class<R>, timeout: Int): R {
        val startTime = System.currentTimeMillis()
        while (true) {

            if (System.currentTimeMillis() - startTime > timeout)
                throw TimeoutException("No response from '${request.endpoint}'. Timeout: $timeout")

            val response = responses.pop { it.requestId == request.requestId && it.endpoint == request.endpoint }
            response?.run {
                val data = if (this.data != null) JSONObject(this.data as Map<*, *>).toString() else "{}"
                when (this.status) {
                    ResponseStatus.OK -> return moshi.adapter(responseClass).fromJson(data)!!
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
            responses.add(d!!)
        }
    }
}