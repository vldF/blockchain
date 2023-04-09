package me.vldf.blockchain.network

import me.vldf.blockchain.blockchain.BlockchainController
import me.vldf.blockchain.network.models.ApiResult
import me.vldf.blockchain.network.models.Message
import me.vldf.blockchain.network.models.bodies.*
import me.vldf.blockchain.services.platformLogger

class RequestMessageProcessor(private val blockchainController: BlockchainController) {
    private val logger by platformLogger()

    fun processMessageAndGetResponse(message: Message): ApiResult<*> {
        val body = message.body

        return processBody(body)
    }

    private fun processBody(body: AbstractBlockchainMessageBody): ApiResult<*> {
        return when (body) {
            is BlockRequestMessageBody -> processBlockRequestMessageBody(body)
            is HashesListRequestMessageBody -> processHashesListRequestMessageBody(body)
            else -> {
                logger.severe("unknown message type of message $body")
                ApiResult.fromError<Nothing?>("unknown message type of message $body")
            }
        }
    }

    private fun processBlockRequestMessageBody(body: BlockRequestMessageBody): ApiResult<BlockResponseMessageBody> {
        val fromIndex = body.fromIndex
        val toIndex = body.toIndex

        return safeInvoke {
            val blocks = blockchainController.getBlocksByRange(fromIndex, toIndex)
            BlockResponseMessageBody(blocks)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun processHashesListRequestMessageBody(
        body: HashesListRequestMessageBody
    ): ApiResult<HashesListResponseMessageBody> {
        return safeInvoke {
            val hashes = blockchainController.getBlockHashes()
            HashesListResponseMessageBody(hashes)
        }
    }

    private fun <T> safeInvoke(func: () -> T): ApiResult<T> {
        return try {
            ApiResult.fromResult(func())
        } catch (e: Exception) {
            ApiResult.fromError(e.message ?: "unknown error")
        }
    }
}
