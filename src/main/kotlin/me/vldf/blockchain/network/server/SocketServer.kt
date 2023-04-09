package me.vldf.blockchain.network.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.vldf.blockchain.network.RequestMessageProcessor
import me.vldf.blockchain.network.models.Message

class SocketServer(
    private val requestMessageProcessor: RequestMessageProcessor,
    port: Int
) {
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private val socketServer = aSocket(selectorManager).tcp().bind(port = port)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun startServer() {
        coroutineScope.launch {
            startListening()
        }
    }

    private suspend fun startListening() {
        while (!socketServer.isClosed) {
            val socket = socketServer.accept()
            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)

            while (!receiveChannel.isClosedForRead) {
                var message: Message? = null

                receiveChannel.read {
                    val byteArray = it.moveToByteArray()
                    val json = byteArray.decodeToString()
                    message = Json.decodeFromString(Message.serializer(), json)
                }

                val response = requestMessageProcessor.processMessageAndGetResponse(message!!)
                sendChannel.write {
                    val json = Json.encodeToString(response)
                    val byteArray = json.toByteArray()
                    it.put(byteArray)
                }
            }
        }
    }
}
