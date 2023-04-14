package me.vldf.blockchain.tests

import me.vldf.blockchain.blockchain.BlockchainController
import me.vldf.blockchain.models.Block
import me.vldf.blockchain.network.NetworkClientFacade
import me.vldf.blockchain.network.client.NodeDescriptorsProvider
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlockchainControllerTests {
    private val blockHashProvider = BlockHashProvider()
    private val personalBlockHashValidator = PersonalBlockHashValidator(blockHashProvider)

    @Test
    fun `initGenesis() test, only one genesis node created`() {
        val blockchainController = getBlockchainController()

        assertEquals(blockchainController.blockCount, 0)
        blockchainController.initGenesis()
        assertEquals(blockchainController.blockCount, 1)

        val genesisBlock = blockchainController.lastBlock
        assertEquals(genesisBlock.index, 0)
        assertEquals(genesisBlock.prevHash.decodeToString(), "genesis")
    }

    @Test
    fun `addAndValidate() and block is valid, block added`() {
        val blockchainController = getBlockchainController()
        blockchainController.initGenesis()
        val genesisBlock = blockchainController.lastBlock

        val newBlock = getNextValidBlock(genesisBlock)

        val validationResult = blockchainController.validateAndAdd(newBlock)
        assertEquals(validationResult, true)
        assertEquals(blockchainController.blockCount, 2)
    }

    @Test
    fun `addAndValidate() and block is valid, two blocks added`() {
        val blockchainController = getBlockchainController()
        blockchainController.initGenesis()
        val genesisBlock = blockchainController.lastBlock

        val newBlock = getNextValidBlock(genesisBlock)

        val validationResult = blockchainController.validateAndAdd(newBlock)
        assertEquals(validationResult, true)
        assertEquals(blockchainController.blockCount, 2)

        val newBlock2 = getNextValidBlock(newBlock)

        val validationResult2 = blockchainController.validateAndAdd(newBlock2)
        assertEquals(validationResult2, true)
        assertEquals(blockchainController.blockCount, 3)
    }

    @Test
    fun `validateAndAdd() index of new block is less than the last, returns false`() {
        val blockchainController = getBlockchainController()
        blockchainController.initGenesis()
        val genesisBlock = blockchainController.lastBlock

        val nextValidBlock = getNextValidBlock(genesisBlock)
        val nextInvalidBlock = nextValidBlock.copy(index = -1)
        val validationResult = blockchainController.validateAndAdd(nextInvalidBlock)

        assertFalse(validationResult)
    }

    @Test
    fun `validateAndAdd() hash is invalid, returns false`() {
        val blockchainController = getBlockchainController()
        blockchainController.initGenesis()
        val genesisBlock = blockchainController.lastBlock

        val nextValidBlock = getNextValidBlock(genesisBlock)
        val nextInvalidBlock = nextValidBlock.copy(nonce = nextValidBlock.nonce+1)
        val validationResult = blockchainController.validateAndAdd(nextInvalidBlock)

        assertFalse(validationResult)
    }

    @Test
    fun `validate() when only genesis block exists, blockchain is valid`() {
        val blockchainController = getBlockchainController()
        blockchainController.initGenesis()

        assertTrue(blockchainController.validate())
    }

    @Test
    fun `validate() when one block exists, blockchain is valid`() {
        val blockchainController = getBlockchainController()
        val blockchain = blockchainController.getBlockchain()
        blockchainController.initGenesis()
        val genesis = blockchainController.lastBlock

        val newBlock = getNextValidBlock(genesis)
        blockchain.add(newBlock)

        assertTrue(blockchainController.validate())
    }

    @Test
    fun `validate() when two block exists, blockchain is valid`() {
        val blockchainController = getBlockchainController()
        val blockchain = blockchainController.getBlockchain()
        blockchainController.initGenesis()
        val genesis = blockchainController.lastBlock

        val newBlock = getNextValidBlock(genesis)
        blockchain.add(newBlock)

        val newBlock2 = getNextValidBlock(newBlock)
        blockchain.add(newBlock2)

        assertTrue(blockchainController.validate())
    }

    @Test
    fun `validate() when one block has invalid prevHash, blockchain isn't valid`() {
        val blockchainController = getBlockchainController()
        val blockchain = blockchainController.getBlockchain()
        blockchainController.initGenesis()
        val genesis = blockchainController.lastBlock

        val nextValidBlock = getNextValidBlock(genesis)
        val invalidBlock = nextValidBlock.copy(prevHash = byteArrayOf())
        blockchain.add(invalidBlock)

        assertFalse(blockchainController.validate())
    }

    @Test
    fun `validate() when two blocks have the same index, blockchain isn't valid`() {
        val blockchainController = getBlockchainController()
        val blockchain = blockchainController.getBlockchain()
        blockchainController.initGenesis()
        val genesis = blockchain.lastBlock

        val nextValidBlock1 = getNextValidBlock(genesis)
        blockchain.add(nextValidBlock1)

        val nextValidBlock2 = getNextValidBlock(nextValidBlock1).copy(index = 1)
        blockchain.add(nextValidBlock2)

        assertFalse(blockchainController.validate())
    }

    private fun getBlockchainController(): BlockchainController {
        val nodesDescriptorsProvider = NodeDescriptorsProvider()
        val networkClientFacade = NetworkClientFacade(nodesDescriptorsProvider)

        return BlockchainController(
            blockHashProvider = blockHashProvider,
            personalBlockHashValidator = personalBlockHashValidator,
            networkClientFacade = networkClientFacade
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
