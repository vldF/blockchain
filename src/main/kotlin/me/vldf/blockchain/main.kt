package me.vldf.blockchain

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.serialization.InternalSerializationApi
import me.vldf.blockchain.blockchain.BlockchainController
import me.vldf.blockchain.miner.Miner
import me.vldf.blockchain.network.NetworkClientFacade
import me.vldf.blockchain.network.NetworkManager
import me.vldf.blockchain.network.client.BlockchainNodeDescriptor
import me.vldf.blockchain.network.client.NodeDescriptorsProvider
import me.vldf.blockchain.services.BlockDataProvider
import me.vldf.blockchain.services.BlockHashProvider
import me.vldf.blockchain.services.PersonalBlockHashValidator

fun main(args: Array<String>) {
    val argParser = ArgParser("blockchain arguments parser")
    val serverPort by argParser
        .option(ArgType.Int, fullName = "port", shortName = "p", description = "server port")
        .required()
    val disableMining by argParser.option(ArgType.Boolean).default(false)
    val nodesList by argParser.option(ArgType.String, fullName = "nodes", description = "nodes list").required()
    argParser.parse(args)

    val nodeDescriptors = processNodesList(nodesList)

    run(serverPort, disableMining, nodeDescriptors)
}

private fun processNodesList(value: String): List<BlockchainNodeDescriptor> {
    return value.split(",").map { hostWithPort ->
        val (host, port) = hostWithPort.split(":")
        BlockchainNodeDescriptor(
            host = host,
            port = port.toInt()
        )
    }
}

@OptIn(InternalSerializationApi::class)
private fun run(
    serverPort: Int,
    disableMining: Boolean,
    nodeDescriptors: List<BlockchainNodeDescriptor>,
) {
    val nodeDescriptorsProvider = NodeDescriptorsProvider()
    nodeDescriptors.forEach { descriptor ->
        nodeDescriptorsProvider.addNewNode(descriptor)
    }
    val networkClientFacade = NetworkClientFacade(nodeDescriptorsProvider)

    val blockHashProvider = BlockHashProvider()
    val personalBlockHashValidator = PersonalBlockHashValidator(blockHashProvider)
    val blockchainController = BlockchainController(blockHashProvider, personalBlockHashValidator, networkClientFacade)
    val dataProvider = BlockDataProvider()

    blockchainController.syncBlockchainWithAllNodes()
    blockchainController.initGenesis()

    val networkManager = NetworkManager(serverPort = serverPort, blockchainController)
    networkManager.runNetworkSubsystem()

    if (!disableMining) {
        val miner = Miner(
            blockchainController,
            dataProvider,
            blockHashProvider,
            personalBlockHashValidator,
            networkClientFacade
        )
        miner.startMining()
    } else {
        while (true) { Thread.yield() }
    }
}
