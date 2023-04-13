package me.vldf.blockchain.network.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers

class SocketClient {
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private var socket: Socket? = null

    suspend fun startSession(host: String, port: Int) {
        socket = aSocket(selectorManager).tcp().connect(host, port) {
            this.socketTimeout = 10 * 1000 // 10 s
        }
    }

    suspend fun sendDataAndGetJsonResponse(json: String): String {
        var writeChannel: ByteWriteChannel? = null

        try {
            writeChannel = socket!!.openWriteChannel(autoFlush = true)
            writeChannel.writeStringUtf8(json + "\r\n")
            writeChannel.flush()
        } finally {
            writeChannel?.close()
        }

        val readChannel = socket!!.openReadChannel()
        return readChannel.readUTF8Line()!!
    }

    fun stopSession() {
        selectorManager.close()
        socket?.close()
    }
}
