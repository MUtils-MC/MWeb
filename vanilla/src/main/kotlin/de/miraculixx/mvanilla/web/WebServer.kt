package de.miraculixx.mvanilla.web

import de.miraculixx.mvanilla.data.configFolder
import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.data.settings
import de.miraculixx.mvanilla.messages.cmp
import de.miraculixx.mvanilla.messages.plus
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

object WebServer {
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    private lateinit var server: NettyApplicationEngine
    lateinit var publicIP: String
    val tempFolder = File(configFolder, "temp")

    fun startServer() {
        CoroutineScope(Dispatchers.Default).launch {
//            publicIP = URL("http://ifconfig.me/ip").readText().trim()
            publicIP = "localhost"
            val port = settings.port
            consoleAudience.sendMessage(prefix + cmp("Creating web server via $publicIP:$port..."))
            server = embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = false)
        }
    }

    fun stopServer() {
        server.stop(0, 0)
        consoleAudience.sendMessage(prefix + cmp("Web Server stopped."))
    }
}

private fun Application.module() {
    configureDownloads()
}