package me.vldf.blockchain

import me.vldf.blockchain.blockchain.BlockchainController
import me.vldf.blockchain.miner.Miner
import me.vldf.blockchain.network.NetworkManager
import me.vldf.blockchain.services.BlockDataProvider
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator

fun main() {
    val blockHashProvider = BlockHashProvider()
    val personalBlockHashValidator = PersonalBlockHashValidator(blockHashProvider)
    val blockchainController = BlockchainController(blockHashProvider, personalBlockHashValidator)
    val dataProvider = BlockDataProvider()

    blockchainController.initGenesis()

    val networkManager = NetworkManager(serverPort = 1042, blockchainController)
    networkManager.runNetworkSubsystem()

    val miner = Miner(blockchainController, dataProvider, blockHashProvider, personalBlockHashValidator)
    miner.startMining()
}
