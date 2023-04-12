package me.vldf.blockchain.network.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers

class SocketClient {
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private var socket: Socket? = null

    suspend fun startSession(host: String, port: Int) {
        socket = aSocket(selectorManager).tcp().connect(host, port)
    }

    suspend fun sendDataAndGetJsonResponse(json: String): String {
        val writeChannel = socket!!.openWriteChannel(autoFlush = true)
        writeChannel.writeStringUtf8(json + "\r\n")
        writeChannel.flush()

        val readChannel = socket!!.openReadChannel()
        val resultJson = readChannel.readUTF8Line()
        return resultJson!!
    }

    fun stopSession() {
        selectorManager.close()
        socket?.close()
    }
}
