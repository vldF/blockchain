package me.vldf.blockchain.network.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.vldf.blockchain.network.RequestMessageProcessor
import me.vldf.blockchain.network.models.Message
import me.vldf.blockchain.services.platformLogger

@InternalSerializationApi
class SocketServer(
    private val requestMessageProcessor: RequestMessageProcessor,
    port: Int
) {
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private val socketServer = aSocket(selectorManager).tcp().bind(port = port)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val logger by platformLogger()

    fun startServer() {
        coroutineScope.launch {
            startListening()
        }
    }

    private suspend fun startListening() {
        while (true) {
            val socket = socketServer.accept()
            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)

            while (!receiveChannel.isClosedForRead) {
                try {
                    val requestJson = receiveChannel.readUTF8Line()
                    val message = Json.decodeFromString(Message.serializer(), requestJson!!)

                    val response = requestMessageProcessor.processMessageAndGetResponse(message)
                    val jsonResponse = Json.encodeToString(response) + "\r\n"

                    sendChannel.writeStringUtf8(jsonResponse)
                    withContext(Dispatchers.IO) {
                        socket.close()
                    }
                } catch (e: Exception) {
                    logger.warning(e.stackTraceToString())
                }
            }
        }
    }
}
