package me.vldf.blockchain.network

import kotlinx.serialization.json.Json
import me.vldf.blockchain.models.Block
import me.vldf.blockchain.network.client.BlockchainNodeDescriptor
import me.vldf.blockchain.network.client.IdProvider
import me.vldf.blockchain.network.client.NodeDescriptorsProvider
import me.vldf.blockchain.network.client.SocketClient
import me.vldf.blockchain.network.models.Message
import me.vldf.blockchain.network.models.MessageType
import me.vldf.blockchain.network.models.bodies.BlockRequestMessageBody
import me.vldf.blockchain.network.models.bodies.HashesListRequestMessageBody
import me.vldf.blockchain.network.models.bodies.NewBlockMinedBody

class NetworkClientFacade(private val nodeDescriptorsProvider: NodeDescriptorsProvider) {
    private val idProvider = IdProvider()

    private val nodesDescriptors: List<BlockchainNodeDescriptor>
        get() = nodeDescriptorsProvider.getAllKnownNodes()

    suspend fun notifyNewBlockMined(block: Block) {
        val id = idProvider.getNextId()

        val message = Message(
            id = id,
            type = MessageType.NOTIFICATION_NEW_BLOCK_MINED,
            body = NewBlockMinedBody(block)
        )

        sendToEachNode(message)
    }

    suspend fun requestBlockFromEachNode(fromIndex: Int, toIndex: Int): Int {
        val id = idProvider.getNextId()

        val message = Message(
            id = id,
            type = MessageType.REQUEST_BLOCKS,
            body = BlockRequestMessageBody(fromIndex, toIndex)
        )

        sendToEachNode(message)

        return id
    }

    suspend fun requestHashesListFromEachNode(): Int {
        val id = idProvider.getNextId()

        val message = Message(
            id = id,
            type = MessageType.REQUEST_HASHES_LIST,
            body = HashesListRequestMessageBody()
        )

        sendToEachNode(message)

        return id
    }

    private suspend fun sendToEachNode(message: Message) {
        nodesDescriptors.forEach { descriptor ->
            descriptor.sendMessage(message)
        }
    }

    private suspend fun BlockchainNodeDescriptor.sendMessage(message: Message) {
        val client = SocketClient()
        client.startSession(this.host, this.port)

        val messageSerialized = message.serialize()
        client.sendData(messageSerialized)

        client.stopSession()
    }

    private fun Message.serialize(): ByteArray {
        val json = Json.encodeToString(Message.serializer(), this)
        return json.toByteArray()
    }
}
