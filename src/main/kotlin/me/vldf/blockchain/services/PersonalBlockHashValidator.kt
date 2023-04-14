package me.vldf.blockchain.services

import me.vldf.blockchain.models.Block
import kotlin.experimental.and

class PersonalBlockHashValidator(private val blockHashProvider: BlockHashProvider) {
    fun validateBlockHash(block: Block): Boolean {
        val hash = blockHashProvider.computeHash(block)
        return isHashValid(hash)
    }

    fun isHashValid(hash: ByteArray): Boolean {
        val firstByte = hash[hash.size - 1]
        val secondByte = hash[hash.size - 2]
        val thirdByte = hash[hash.size - 3]

        return  firstByte == 0.toByte() && secondByte == 0.toByte() && isByteMatchesMask(thirdByte)
    }

    private fun isByteMatchesMask(byte: Byte): Boolean {
        return (byte and validHashMask) == 0.toByte()
    }

    companion object {
        private const val validHashMask: Byte = 0b00000111.toByte()
    }
}
