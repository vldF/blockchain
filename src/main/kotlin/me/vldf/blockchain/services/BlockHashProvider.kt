package me.vldf.blockchain.services

import me.vldf.blockchain.models.Block
import java.nio.ByteBuffer
import java.security.MessageDigest

class BlockHashProvider {
    fun computeHash(block: Block): ByteArray {
        return computeHash(block.index, block.prevHash, block.data, block.nonce)
    }

    fun computeHash(index: Int, prevHash: ByteArray, data: ByteArray, nonce: Int): ByteArray {
        val shaDigest = MessageDigest.getInstance("SHA-256")

        val indexByteArray = ByteBuffer.allocate(4).putInt(index)
        val nonceByteArray = ByteBuffer.allocate(4).putInt(nonce)

        shaDigest.update(indexByteArray.array())
        shaDigest.update(nonceByteArray.array())
        shaDigest.update(data)
        shaDigest.update(prevHash)

        return shaDigest.digest()
    }
}
