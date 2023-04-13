package me.vldf.blockchain.blockchain

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.InternalSerializationApi
import me.vldf.blockchain.models.Block
import me.vldf.blockchain.network.NetworkClientFacade
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator
import me.vldf.blockchain.services.platformLogger
import org.jetbrains.annotations.TestOnly
import java.util.logging.Logger

@InternalSerializationApi
class BlockchainController(
    private val blockHashProvider: BlockHashProvider,
    private val personalBlockHashValidator: PersonalBlockHashValidator,
    private val networkClientFacade: NetworkClientFacade,
) {
    private val blockchain = Blockchain()
    private var isValidationErrorProcessing = false

    private val logger by platformLogger()

    fun validate(): Boolean {
        return validateBlocks(blockchain.blocks, blockHashProvider, logger)
    }

    fun validateAndAdd(block: Block): Boolean {
        if (block.index != 0 &&  !personalBlockHashValidator.validateBlockHash(block)) {
            logger.severe("validation of a single block failed: hash isn't valid $block")
            onValidationError()
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
            onValidationError()
            return false
        }

        if (block.index - lastSavedBlock.index > 1) {
            logger.info("skipped too many blocks")
            onValidationError()
            return false
        }

        if (lastSavedBlock.index == block.index) {
            onValidationError()
            return false
        }

        val lastSavedBlockHash = blockHashProvider.computeHash(lastSavedBlock)
        if (!lastSavedBlockHash.contentEquals(block.prevHash)) {
            logger.severe(
                "validation of a single block failed: hash mismatch: " +
                        "expected $lastSavedBlockHash, but actual ${block.prevHash}"
            )
            onValidationError()
            return false
        }

        blockchain.add(block)
        logger.info("added new block with index ${block.index}")

        return true
    }

    private fun onValidationError() {
        runBlocking {
            logger.info("validation error handling")

            val lastCommonIndex = getLastCommonBlockIndex()
            val newBlocks = networkClientFacade.requestActualBlockchain(lastCommonIndex)

            combineBlockchainsAndReplace(newBlocks)

            logger.info("validation error handling is done")
        }
    }

    private fun getLastCommonBlockIndex(): Int {
        return runBlocking {
            val remoteChainHashesVariants = networkClientFacade
                .requestHashesListFromEachNode()
                .mapNotNull { apiResult -> apiResult.result }
                .map { responseBody -> responseBody.hashes }

            val localHashes = blockchain.blocks.map { block -> blockHashProvider.computeHash(block) }

            val allBlockchainVariants = remoteChainHashesVariants + listOf(localHashes)

            val minChainSize = allBlockchainVariants.minOf { chain -> chain.size }
            var lastCommonBlockIndex = 0
            for (i in 0 until minChainSize) {
                val currentBlockInEveryChain = allBlockchainVariants.map { chain -> chain[i] }
                val areBlockTheSame = setOf(currentBlockInEveryChain).size == 1
                if (areBlockTheSame) {
                    lastCommonBlockIndex = i
                }
            }

            lastCommonBlockIndex
        }
    }

    fun syncBlockchainWithAllNodes() {
        runBlocking {
            val blocks = networkClientFacade.requestActualBlockchain(startIndex = 0)
            for (block in blocks) {
                validateAndAdd(block)
            }
        }
    }

    private fun combineBlockchainsAndReplace(newBlockchain: List<Block>) {
        val firstNewBlockIndex = newBlockchain.firstOrNull()?.index ?: return
        val lastValidBlockIndex = firstNewBlockIndex - 1
        val oldValidBlocksList = blockchain.blocks

        val newBlockChain = oldValidBlocksList.subList(0, lastValidBlockIndex + 1) + newBlockchain
        if (validateBlocks(newBlockChain, blockHashProvider, logger)) {
            replaceBlockchain(newBlockchain)
        }
    }

    private fun replaceBlockchain(newBlockchain: List<Block>) {
        blockchain.replace(newBlockchain)
    }

    val lastBlock: Block
        get() = blockchain.lastBlock

    val blockCount: Int
        get() = blockchain.blockCount

    @Throws(IllegalArgumentException::class)
    fun getBlocksByRange(from: Int, to: Int): List<Block> {
        check(from <= to || to == 0) { "the start index must be less than or equal to the end index" }
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

    private companion object {
        fun validateBlocks(blocks: List<Block>, blockHashProvider: BlockHashProvider, logger: Logger): Boolean {
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
    }
}
