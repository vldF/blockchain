package me.vldf.blockchain.network.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers

class SocketClient {
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private lateinit var socket: Socket

    suspend fun startSession(host: String, port: Int) {
        socket = aSocket(selectorManager).tcp().connect(host, port)
    }

    suspend fun sendData(data: ByteArray) {
        socket.openWriteChannel(autoFlush = true).writeFully(data)
    }

    fun stopSession() {
        socket.close()
    }
}
