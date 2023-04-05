package me.vldf.blockchain.services

import java.util.logging.LogManager
import java.util.logging.Logger

fun <T : Any> T.platformLogger(): Lazy<Logger> {
    return lazy { PlatformLogger.getLogger(this.javaClass) }
}

private object PlatformLogger {
    private val logManager: LogManager = LogManager.getLogManager()

    init {
        logManager.readConfiguration(javaClass.classLoader.getResourceAsStream("logging.properties"))
    }

    fun getLogger(klass: Class<*>): Logger {
        return Logger.getLogger(klass.name)
    }
}
