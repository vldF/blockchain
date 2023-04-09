package me.vldf.blockchain.network

import me.vldf.blockchain.blockchain.BlockchainController

class NetworkManager(
    private val serverPort: Int,
    private val blockchainController: BlockchainController
) {
    fun runNetworkSubsystem() {
        val networkServerFacade = NetworkServerFacade(blockchainController)
        networkServerFacade.startServer(serverPort)
    }
}
