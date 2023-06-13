package de.miraculixx.mvanilla.web

import de.miraculixx.mvanilla.data.configFolder
import de.miraculixx.mvanilla.data.consoleAudience
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.data.settings
import de.miraculixx.mvanilla.messages.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object WebServer {
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    val jsonFull = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }
    private lateinit var server: ApplicationEngine
    var publicIP = "localhost"
    val tempFolder = File(configFolder, "temp")
    var isStarted = false
    var isOutdated = false
    var outdatedMessage = emptyComponent()

    fun checkVersion(currentVersion: Int): Boolean {
        val version = try {
            val url = URL("https://api.mutils.de/public/version")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET";
            con.setRequestProperty("User-Agent", "MUtils-API-1.1")
            con.setRequestProperty("Service", "MUtils-Web")
            con.doInput = true;
            con.doOutput = true;
            con.connect()
            json.decodeFromString<Version>(con.inputStream.readBytes().decodeToString())
        } catch (e: Exception) {
            null
        }
        if (version == null) {
            consoleAudience.sendMessage(prefix + cmp("Could not check current version! Proceed at your own risk", cError))
            return true
        }
        outdatedMessage = cmp("Latest Version: ") + cmp(version.latest.toString(), cSuccess) + cmp(" - Installed Version: ") + cmp(currentVersion.toString(), cError)
        if (currentVersion < version.last) {
            consoleAudience.sendMessage(prefix + cmp("You are running a too outdated version of MWeb! An update is required due to security reasons or internal changes.", cError))
            consoleAudience.sendMessage(prefix + outdatedMessage)
            isOutdated = true
            return false
        }
        if (currentVersion < version.latest) {
            consoleAudience.sendMessage(prefix + cmp("You are running an outdated version of MWeb!"))
            consoleAudience.sendMessage(prefix + outdatedMessage)
            isOutdated = true
        }
        if (currentVersion > version.latest) {
            consoleAudience.sendMessage(prefix + cmp("You are running a beta version. Bugs may appear!"))
        }
        return true
    }

    fun startServer() {
        CoroutineScope(Dispatchers.Default).launch {
            publicIP = URL("http://ifconfig.me/ip").readText().trim()
            val port = settings.port
            consoleAudience.sendMessage(prefix + cmp("Creating web server via $publicIP:$port..."))
            server = embeddedServer(CIO, port = port, host = "0.0.0.0", module = Application::module).start(wait = false)
            isStarted = true
        }
    }

    fun stopServer() {
        server.stop(0, 0)
        consoleAudience.sendMessage(prefix + cmp("Web Server stopped."))
    }

    @Serializable
    private data class Version(val latest: Int, val last: Int)
}

private fun Application.module() {
    configureDownloads()
}