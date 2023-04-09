package me.vldf.blockchain.network.client

class NodeDescriptorsProvider {
    private val clients = mutableListOf<BlockchainNodeDescriptor>()

    fun addNewNode(host: String, port: Int) {
        val descriptor = BlockchainNodeDescriptor(host, port)
        clients.add(descriptor)
    }

    fun getAllKnownNodes(): List<BlockchainNodeDescriptor> {
        return clients.toList()
    }
}
