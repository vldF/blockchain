package me.vldf.blockchain.models

import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator
import me.vldf.blockchain.services.platformLogger
import org.jetbrains.annotations.TestOnly

class Blockchain(
    private val blockHashProvider: BlockHashProvider,
    private val personalBlockHashValidator: PersonalBlockHashValidator,
) {
    private val blocks = mutableListOf<Block>()

    private val logger by platformLogger()

    fun validate(): Boolean {
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

        add(block)

        return true
    }

    fun add(block: Block) {
        blocks.add(block)
    }

    fun getLastBlock(): Block {
        return blocks.last()
    }

    @TestOnly
    fun getBlockCount(): Int {
        return blocks.size
    }

    fun initGenesis() {
        val block = Block(
            index = 0,
            prevHash = "genesis".toByteArray(),
            data = "genesis-block".toByteArray(),
            nonce = 0
        )

        add(block)
    }
}
