package me.vldf.blockchain.network.client

class NodeDescriptorsProvider {
    private val clients = mutableListOf<BlockchainNodeDescriptor>()

    fun addNewNode(descriptor: BlockchainNodeDescriptor) {
        clients.add(descriptor)
    }

    fun getAllKnownNodes(): List<BlockchainNodeDescriptor> {
        return clients.toList()
    }
}
