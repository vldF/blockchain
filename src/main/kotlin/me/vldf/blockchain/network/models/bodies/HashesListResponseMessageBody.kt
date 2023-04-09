package me.vldf.blockchain.network.models.bodies

import kotlinx.serialization.Serializable

@Serializable
data class HashesListResponseMessageBody(
    val hashes: List<ByteArray>
) : AbstractBlockchainMessageBody()
