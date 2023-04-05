package me.vldf.blockchain.models

data class Block(
    val index: Int,
    val prevHash: ByteArray,
    val data: ByteArray,
    val nonce: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Block) return false

        if (index != other.index) return false
        if (!prevHash.contentEquals(other.prevHash)) return false
        if (!data.contentEquals(other.data)) return false
        if (nonce != other.nonce) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + prevHash.contentHashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + nonce
        return result
    }
}
