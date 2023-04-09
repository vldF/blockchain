package me.vldf.blockchain.blockchain

import me.vldf.blockchain.models.Block
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator
import me.vldf.blockchain.services.platformLogger
import org.jetbrains.annotations.TestOnly

class BlockchainController(
    private val blockHashProvider: BlockHashProvider,
    private val personalBlockHashValidator: PersonalBlockHashValidator,
) {
    private val blockchain = Blockchain()

    private val logger by platformLogger()

    fun validate(): Boolean {
        val blocks = blockchain.blocks
        for (block in blocks) {
            if (block.index == 0) {
                // genesis block
                continue
            }

            val index = block.index
            val previousBlock = blocks[index - 1]
            if (!blockHashProvider.computeHash(previousBlock).contentEquals(block.prevHash)) {
                logger.severe("Block validation failed for block $block")
                return false
            }
        }

        return true
    }

    fun validateAndAdd(block: Block): Boolean {
        if (!personalBlockHashValidator.validateBlockHash(block)) {
            logger.severe("validation of a single block failed: hash isn't valid $block")
            return false
        }

        val blocks = blockchain.blocks
        val lastSavedBlock = blocks.last()
        if (lastSavedBlock.index > block.index) {
            logger.severe("validation of a single block failed: block is too old $block")
            return false
        }

        if (lastSavedBlock.index == block.index) {
            TODO()
        }

        val lastSavedBlockHash = blockHashProvider.computeHash(lastSavedBlock)
        if (!lastSavedBlockHash.contentEquals(block.prevHash)) {
            logger.severe(
                "validation of a single block failed: hash mismatch: " +
                        "expected $lastSavedBlockHash, but actual ${block.prevHash}"
            )
            return false
        }

        blockchain.add(block)

        return true
    }

    val lastBlock: Block
        get() = blockchain.lastBlock

    val blockCount: Int
        get() = blockchain.blockCount

    @Throws(IllegalArgumentException::class)
    fun getBlocksByRange(from: Int, to: Int): List<Block> {
        check(from <= to) { "the start index must be less than or equal to the end index" }
        check(from >= 0) { "the start index must be non-negative" }
        check(to <= blockchain.blockCount) { "the end index must be less than or equal to block count" }

        return blockchain.blocks.subList(from, to)
    }

    fun getBlockHashes(): List<ByteArray> {
        return blockchain.blocks.map { block ->
            blockHashProvider.computeHash(block)
        }
    }

    fun initGenesis() {
        val block = Block(
            index = 0,
            prevHash = "genesis".toByteArray(),
            data = "genesis-block".toByteArray(),
            nonce = 0
        )

        blockchain.add(block)
    }

    @TestOnly
    fun getBlockchain(): Blockchain {
        return blockchain
    }
}
