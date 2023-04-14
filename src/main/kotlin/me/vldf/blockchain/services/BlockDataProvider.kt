package me.vldf.blockchain.services

import kotlin.random.Random

class BlockDataProvider(seed: Int = 42) {
    private val dataLength = 256

    private val random = Random

    fun getData(): ByteArray {
        val data = ByteArray(dataLength)
        return random.nextBytes(data)
    }
}
