package me.vldf.blockchain.network.client

class IdProvider {
    @Volatile
    private var currentId = 0

    fun getNextId(): Int {
        return currentId++
    }
}
