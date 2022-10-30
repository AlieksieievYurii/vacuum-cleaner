package com.yurii.vaccumcleaner.utils.requesthandler

interface Communicator {
    fun read(): String
    fun send(data: String)
    fun close()
}