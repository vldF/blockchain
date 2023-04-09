package me.vldf.blockchain.network.models

enum class MessageType {
    NOTIFICATION_NEW_BLOCK_MINED,

    REQUEST_HASHES_LIST, REQUEST_BLOCKS,
}
