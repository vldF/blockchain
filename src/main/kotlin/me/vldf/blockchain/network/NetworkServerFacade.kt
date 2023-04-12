package me.vldf.blockchain.network

import kotlinx.serialization.InternalSerializationApi
import me.vldf.blockchain.blockchain.BlockchainController
import me.vldf.blockchain.network.server.SocketServer

@InternalSerializationApi
class NetworkServerFacade(blockchainController: BlockchainController) {
    private lateinit var server: SocketServer
    private val requestMessageProcessor = RequestMessageProcessor(blockchainController)

    fun startServer(port: Int) {
        server = SocketServer(requestMessageProcessor, port)
        server.startServer()
    }
}
