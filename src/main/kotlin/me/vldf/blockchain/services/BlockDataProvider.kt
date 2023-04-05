package me.vldf.blockchain.services

import kotlin.random.Random

class BlockDataProvider(seed: Int = 42) {
    private val dataLength = 256

    private val random = Random(seed)

    fun getData(): ByteArray {
        val data = ByteArray(dataLength)
        return random.nextBytes(data)
    }
}
