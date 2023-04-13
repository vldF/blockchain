package me.vldf.blockchain.network.models.bodies

import kotlinx.serialization.Serializable
import me.vldf.blockchain.models.Block

@Serializable
data class BlockResponseMessageBody(
    val blocks: List<Block>
) : AbstractBlockchainMessageBody()
