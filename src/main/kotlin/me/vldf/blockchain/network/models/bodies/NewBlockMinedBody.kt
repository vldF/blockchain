package me.vldf.blockchain.network.models.bodies

import kotlinx.serialization.Serializable
import me.vldf.blockchain.models.Block

@Serializable
data class NewBlockMinedBody(
    val block: Block
) : AbstractBlockchainMessageBody()
