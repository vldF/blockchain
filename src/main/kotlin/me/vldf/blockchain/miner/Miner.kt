package me.vldf.blockchain.miner

import me.vldf.blockchain.models.Block
import me.vldf.blockchain.models.Blockchain
import me.vldf.blockchain.services.BlockDataProvider
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator
import me.vldf.blockchain.services.platformLogger

class Miner(
    private val blockchain: Blockchain,
    private val dataProvider: BlockDataProvider,
    private val blockHashProvider: BlockHashProvider,
    private val personalBlockHashValidator: PersonalBlockHashValidator,
) {
    private val logger by platformLogger()

    private var state: MinerState = MinerState.Paused

    fun startMining() {
        state = MinerState.Run
        while (true) {
            mineNext()
        }
    }

    private fun mineNext() {
        val nextBlockData = dataProvider.getData()

        val lastBlock = blockchain.getLastBlock()
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

    private fun onNewBlockMined(block: Block) {
        logger.info("new block #${block.index} mined")
        blockchain.validateAndAdd(block)
    }
}
