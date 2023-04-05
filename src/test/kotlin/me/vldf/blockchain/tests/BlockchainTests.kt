package me.vldf.blockchain.tests

import me.vldf.blockchain.models.Block
import me.vldf.blockchain.models.Blockchain
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlockchainTests {
    private val blockHashProvider = BlockHashProvider()
    private val personalBlockHashValidator = PersonalBlockHashValidator(blockHashProvider)

    @Test
    fun `initGenesis() test, only one genesis node created`() {
        val blockchain = getBlockchain()

        assertEquals(blockchain.getBlockCount(), 0)
        blockchain.initGenesis()
        assertEquals(blockchain.getBlockCount(), 1)

        val genesisBlock = blockchain.getLastBlock()
        assertEquals(genesisBlock.index, 0)
        assertEquals(genesisBlock.prevHash.decodeToString(), "genesis")
    }

    @Test
    fun `addAndValidate() and block is valid, block added`() {
        val blockchain = getBlockchain()
        blockchain.initGenesis()
        val genesisBlock = blockchain.getLastBlock()

        val newBlock = getNextValidBlock(genesisBlock)

        val validationResult = blockchain.validateAndAdd(newBlock)
        assertEquals(validationResult, true)
        assertEquals(blockchain.getBlockCount(), 2)
    }

    @Test
    fun `addAndValidate() and block is valid, two blocks added`() {
        val blockchain = getBlockchain()
        blockchain.initGenesis()
        val genesisBlock = blockchain.getLastBlock()

        val newBlock = getNextValidBlock(genesisBlock)

        val validationResult = blockchain.validateAndAdd(newBlock)
        assertEquals(validationResult, true)
        assertEquals(blockchain.getBlockCount(), 2)

        val newBlock2 = getNextValidBlock(newBlock)

        val validationResult2 = blockchain.validateAndAdd(newBlock2)
        assertEquals(validationResult2, true)
        assertEquals(blockchain.getBlockCount(), 3)
    }

    @Test
    fun `validateAndAdd() index of new block is less than the last, returns false`() {
        val blockchain = getBlockchain()
        blockchain.initGenesis()
        val genesisBlock = blockchain.getLastBlock()

        val nextValidBlock = getNextValidBlock(genesisBlock)
        val nextInvalidBlock = nextValidBlock.copy(index = -1)
        val validationResult = blockchain.validateAndAdd(nextInvalidBlock)

        assertFalse(validationResult)
    }

    @Test
    fun `validateAndAdd() hash is invalid, returns false`() {
        val blockchain = getBlockchain()
        blockchain.initGenesis()
        val genesisBlock = blockchain.getLastBlock()

        val nextValidBlock = getNextValidBlock(genesisBlock)
        val nextInvalidBlock = nextValidBlock.copy(nonce = nextValidBlock.nonce+1)
        val validationResult = blockchain.validateAndAdd(nextInvalidBlock)

        assertFalse(validationResult)
    }

    @Test
    fun `validate() when only genesis block exists, blockchain is valid`() {
        val blockchain = getBlockchain()
        blockchain.initGenesis()

        assertTrue(blockchain.validate())
    }

    @Test
    fun `validate() when one block exists, blockchain is valid`() {
        val blockchain = getBlockchain()
        blockchain.initGenesis()
        val genesis = blockchain.getLastBlock()

        val newBlock = getNextValidBlock(genesis)
        blockchain.add(newBlock)

        assertTrue(blockchain.validate())
    }

    @Test
    fun `validate() when two block exists, blockchain is valid`() {
        val blockchain = getBlockchain()
        blockchain.initGenesis()
        val genesis = blockchain.getLastBlock()

        val newBlock = getNextValidBlock(genesis)
        blockchain.add(newBlock)

        val newBlock2 = getNextValidBlock(newBlock)
        blockchain.add(newBlock2)

        assertTrue(blockchain.validate())
    }

    @Test
    fun `validate() when one block has invalid prevHash, blockchain isn't valid`() {
        val blockchain = getBlockchain()
        blockchain.initGenesis()
        val genesis = blockchain.getLastBlock()

        val nextValidBlock = getNextValidBlock(genesis)
        val invalidBlock = nextValidBlock.copy(prevHash = byteArrayOf())
        blockchain.add(invalidBlock)

        assertFalse(blockchain.validate())
    }

    @Test
    fun `validate() when two blocks have the same index, blockchain isn't valid`() {
        val blockchain = getBlockchain()
        blockchain.initGenesis()
        val genesis = blockchain.getLastBlock()

        val nextValidBlock1 = getNextValidBlock(genesis)
        blockchain.add(nextValidBlock1)

        val nextValidBlock2 = getNextValidBlock(nextValidBlock1).copy(index = 1)
        blockchain.add(nextValidBlock2)

        assertFalse(blockchain.validate())
    }

    private fun getBlockchain(): Blockchain {
        return Blockchain(
            blockHashProvider = blockHashProvider,
            personalBlockHashValidator = personalBlockHashValidator
        )
    }

    private fun getNextValidBlock(previousValidBlock: Block): Block {
        var nonce = 0

        while (true) {
            val block = Block(
                index = previousValidBlock.index + 1,
                prevHash = blockHashProvider.computeHash(previousValidBlock),
                data = byteArrayOf(),
                nonce = nonce
            )

            val isValid = personalBlockHashValidator.isHashValid(blockHashProvider.computeHash(block))
            if (isValid) {
                return block
            }

            nonce++
        }
    }
}
