package me.vldf.blockchain.miner

import kotlinx.serialization.InternalSerializationApi
import me.vldf.blockchain.blockchain.BlockchainController
import me.vldf.blockchain.models.Block
import me.vldf.blockchain.network.NetworkClientFacade
import me.vldf.blockchain.services.BlockDataProvider
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator
import me.vldf.blockchain.services.platformLogger

@InternalSerializationApi
class Miner(
    private val blockchainController: BlockchainController,
    private val dataProvider: BlockDataProvider,
    private val blockHashProvider: BlockHashProvider,
    private val personalBlockHashValidator: PersonalBlockHashValidator,
    private val networkClientFacade: NetworkClientFacade,
) {
    private val logger by platformLogger()

    init {
        blockchainController.addMinerRestartListener { toRestartState() }
    }

    private var state: MinerState = MinerState.Restart

    fun startMining() {
        while (true) {
            state = MinerState.Run
            mineNext()
        }
    }

    private fun mineNext() {
        val nextBlockData = dataProvider.getData()

        val lastBlock = blockchainController.lastBlock
        val prevHash = blockHashProvider.computeHash(lastBlock)
        val index = lastBlock.index + 1

        var nonce = 0

        while (state == MinerState.Run) {
            val newHash = blockHashProvider.computeHash(index, prevHash, nextBlockData, nonce)
            val isHashValid = personalBlockHashValidator.isHashValid(newHash)

            if (isHashValid) {
                val block = Block(index, prevHash, nextBlockData, nonce)
                onNewBlockMined(block)

                return
            }

            nonce++
        }
    }

    private fun toRestartState() {
        state = MinerState.Restart
    }

    private fun onNewBlockMined(block: Block) {
        logger.info("new block #${block.index} mined")
        val result = blockchainController.validateAndAdd(block)
        if (!result) {
            return
        }

        networkClientFacade.notifyNewBlockMined(block)
    }
}
