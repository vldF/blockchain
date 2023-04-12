package me.vldf.blockchain.network

import kotlinx.serialization.InternalSerializationApi
import me.vldf.blockchain.blockchain.BlockchainController

@InternalSerializationApi
class NetworkManager(
    private val serverPort: Int,
    private val blockchainController: BlockchainController
) {
    fun runNetworkSubsystem() {
        val networkServerFacade = NetworkServerFacade(blockchainController)
        networkServerFacade.startServer(serverPort)
    }
}
