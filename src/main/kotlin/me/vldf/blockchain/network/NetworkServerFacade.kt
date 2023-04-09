package me.vldf.blockchain.network

import me.vldf.blockchain.blockchain.BlockchainController
import me.vldf.blockchain.network.server.SocketServer

class NetworkServerFacade(blockchainController: BlockchainController) {
    private lateinit var server: SocketServer
    private val requestMessageProcessor = RequestMessageProcessor(blockchainController)

    fun startServer(port: Int) {
        server = SocketServer(requestMessageProcessor, port)
        server.startServer()
    }
}
