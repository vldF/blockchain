package me.vldf.blockchain.network.models.bodies

import kotlinx.serialization.Serializable

@Serializable
data class BlockRequestMessageBody(
    val fromIndex: Int,
    val toIndex: Int
) : AbstractBlockchainMessageBody()
