package de.miraculixx.mvanilla.data

data class Settings(
    var port: Int = 25560,
    var logAccess: Boolean = true,
    var debug: Boolean = false,
    var lang: String = "en_US"
)