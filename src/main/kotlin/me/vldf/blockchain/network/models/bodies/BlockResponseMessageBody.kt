package me.vldf.blockchain.network.models.bodies

import me.vldf.blockchain.models.Block

data class BlockResponseMessageBody(
    val blocks: List<Block>
) : AbstractBlockchainMessageBody()
