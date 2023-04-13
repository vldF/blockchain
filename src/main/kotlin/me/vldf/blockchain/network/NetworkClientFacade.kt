package me.vldf.blockchain.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.vldf.blockchain.models.Block
import me.vldf.blockchain.network.client.BlockchainNodeDescriptor
import me.vldf.blockchain.network.client.IdProvider
import me.vldf.blockchain.network.client.NodeDescriptorsProvider
import me.vldf.blockchain.network.client.SocketClient
import me.vldf.blockchain.network.models.ApiResult
import me.vldf.blockchain.network.models.Message
import me.vldf.blockchain.network.models.MessageType
import me.vldf.blockchain.network.models.bodies.*
import me.vldf.blockchain.services.platformLogger
import java.net.ConnectException
import java.net.SocketTimeoutException

@InternalSerializationApi
class NetworkClientFacade(private val nodeDescriptorsProvider: NodeDescriptorsProvider) {
    private val idProvider = IdProvider()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val logger by platformLogger()
    private val json = Json {
        this.ignoreUnknownKeys = true
    }

    private val nodesDescriptors: List<BlockchainNodeDescriptor>
        get() = nodeDescriptorsProvider.getAllKnownNodes()

    fun notifyNewBlockMined(block: Block) {
        coroutineScope.launch {
            val id = idProvider.getNextId()

            val message = Message(
                id = id,
                type = MessageType.NOTIFICATION_NEW_BLOCK_MINED,
                body = NewBlockMinedBody(block)
            )

            sendToEachNode<EmptyMessageBody>(message)
        }
    }

    fun requestBlockFromEachNode(fromIndex: Int, toIndex: Int) {
        coroutineScope.launch {
            val id = idProvider.getNextId()

            val message = Message(
                id = id,
                type = MessageType.REQUEST_BLOCKS,
                body = BlockRequestMessageBody(fromIndex, toIndex)
            )

            sendToEachNode<BlockResponseMessageBody>(message)
        }
    }

    suspend fun requestActualBlockchain(startIndex: Int): List<Block> {
        val id = idProvider.getNextId()

        val message = Message(
            id = id,
            type = MessageType.REQUEST_BLOCKS,
            body = BlockRequestMessageBody(startIndex, 0)
        )

        val allChains = sendToEachNode<BlockResponseMessageBody>(message)
        val blocks = allChains.mapNotNull { chain -> chain.result }

        return blocks.maxByOrNull { it.blocks.size }?.blocks.orEmpty()
    }

    suspend fun requestHashesListFromEachNode(): List<ApiResult<HashesListResponseMessageBody>> {
        val id = idProvider.getNextId()

        val message = Message(
            id = id,
            type = MessageType.REQUEST_HASHES_LIST,
            body = HashesListRequestMessageBody()
        )

        return sendToEachNode(message)
    }

    private suspend inline fun <reified T : AbstractBlockchainMessageBody> sendToEachNode(message: Message): List<ApiResult<T>> {
        return nodesDescriptors.map { descriptor ->
            descriptor.sendMessage(message)
        }
    }

    private suspend inline fun <reified T: AbstractBlockchainMessageBody> BlockchainNodeDescriptor.sendMessage(message: Message): ApiResult<T> {
        val client = SocketClient()

        return try {
            client.startSession(this.host, this.port)

            val messageSerialized = message.serialize()
            val resultJson = client.sendDataAndGetJsonResponse(messageSerialized)

            resultJson.deserialize()
        } catch (e: ConnectException) {
            ApiResult.fromError(e.message ?: "unknown error")
        } catch (_: SocketTimeoutException) {
            ApiResult.fromError("timeout")
        } catch (e: Exception) {
            logger.info(e.stackTraceToString())
            ApiResult.fromError(e.message ?: "unknown error")
        } finally {
            client.stopSession()
        }
    }

    private inline fun <reified T : AbstractBlockchainMessageBody> String.deserialize(): ApiResult<T> {
        return json.decodeFromString(ApiResult.serializer(T::class.serializer()), this)
    }

    private fun Message.serialize(): String {
        return this@NetworkClientFacade.json.encodeToString(Message.serializer(), this)
    }
}
