package me.vldf.blockchain.blockchain

import me.vldf.blockchain.models.Block

class Blockchain {
    private val blocksInternal = mutableListOf<Block>()

    fun add(block: Block) {
        blocksInternal.add(block)
    }

    val blocks: List<Block>
        get() = blocksInternal.toList()

    val lastBlock: Block
        get() = blocks.last()

    val blockCount: Int
        get() = blocks.size
}
