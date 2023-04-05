package me.vldf.blockchain

import me.vldf.blockchain.miner.Miner
import me.vldf.blockchain.models.Blockchain
import me.vldf.blockchain.services.BlockDataProvider
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator

fun main() {
    val blockHashProvider = BlockHashProvider()
    val personalBlockHashValidator = PersonalBlockHashValidator(blockHashProvider)
    val blockchain = Blockchain(blockHashProvider, personalBlockHashValidator)
    val dataProvider = BlockDataProvider()

    blockchain.initGenesis()

    val miner = Miner(blockchain, dataProvider, blockHashProvider, personalBlockHashValidator)
    miner.startMining()
}
