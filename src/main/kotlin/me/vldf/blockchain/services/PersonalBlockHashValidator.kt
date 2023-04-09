package me.vldf.blockchain.services

import me.vldf.blockchain.models.Block
import kotlin.experimental.and

class PersonalBlockHashValidator(private val blockHashProvider: BlockHashProvider) {
    fun validateBlockHash(block: Block): Boolean {
        val hash = blockHashProvider.computeHash(block)
        return isHashValid(hash)
    }

    fun isHashValid(hash: ByteArray): Boolean {
        val lastByte = hash.last()
        return (lastByte and miningComplexity) == 0.toByte()
    }

    companion object {
        private const val miningComplexity: Byte = 0x11111111.toByte() // todo
    }
}
