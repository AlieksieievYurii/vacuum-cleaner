package com.yurii.vaccumcleaner.requesthandler

interface Communicator {
    fun read(): String
    fun send(data: String)
}