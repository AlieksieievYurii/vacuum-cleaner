package com.yurii.vaccumcleaner.utils.requesthandler

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.yurii.vaccumcleaner.utils.pop
import com.yurii.vaccumcleaner.utils.synchronizedAppend
import kotlinx.coroutines.*
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread


class RequestHandler(private val communicator: Communicator) {
    private var moshi = Moshi.Builder().build()
    private val jsonAdapter = moshi.adapter<Request<*>>(Types.newParameterizedType(Request::class.java, Any::class.java))
    private val responseAdapter = moshi.adapter(Response::class.java)
    private val responses = mutableListOf<Response>()

    private var isStopped = false

    fun start() {
        thread(start = true) { listenForIncomingResponses() }
    }

    fun stop() {
        isStopped = true
        communicator.close()
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
            val data = try {
                communicator.read()
            } catch (error: IOException) {
                if (isStopped)
                    break
                else throw error
            }
            Timber.d(data)

            val parsedData = responseAdapter.fromJson(data)
            responses.synchronizedAppend(this, parsedData ?: throw IllegalStateException("Could not parse the data: $data"))
        }
    }
}