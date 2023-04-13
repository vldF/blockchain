package me.vldf.blockchain.network.models

import kotlinx.serialization.Serializable
import me.vldf.blockchain.network.models.bodies.AbstractBlockchainMessageBody

@Serializable
data class Message (
    val id: Int,
    val type: MessageType,
    val body: AbstractBlockchainMessageBody
)
