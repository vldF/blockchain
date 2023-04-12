package me.vldf.blockchain.blockchain

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.InternalSerializationApi
import me.vldf.blockchain.models.Block
import me.vldf.blockchain.network.NetworkClientFacade
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator
import me.vldf.blockchain.services.platformLogger
import org.jetbrains.annotations.TestOnly

@InternalSerializationApi
class BlockchainController(
    private val blockHashProvider: BlockHashProvider,
    private val personalBlockHashValidator: PersonalBlockHashValidator,
    private val networkClientFacade: NetworkClientFacade,
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
        if (block.index != 0 &&  !personalBlockHashValidator.validateBlockHash(block)) {
            logger.severe("validation of a single block failed: hash isn't valid $block")
            return false
        }

        val blocks = blockchain.blocks
        val lastSavedBlock = blocks.lastOrNull()
        if (lastSavedBlock == null) {
            // genesis node
            blockchain.add(block)
            return true
        }

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
        logger.info("added new block with index ${block.index}")

        return true
    }

    fun syncBlockchainWithAllNodes() {
        runBlocking {
            val blocks = networkClientFacade.requestActualBlockchain()
            for (block in blocks) {
                validateAndAdd(block)
            }
        }
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
        check(to >= 0) { "end index must be non-negative" }

        val blocks = blockchain.blocks

        if (to == 0) {
            return blocks.subList(from, blocks.size)
        }

        return blocks.subList(from, to)
    }

    fun getBlockHashes(): List<ByteArray> {
        return blockchain.blocks.map { block ->
            blockHashProvider.computeHash(block)
        }
    }

    fun initGenesis() {
        if (blockchain.blockCount != 0) {
            return
        }

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
