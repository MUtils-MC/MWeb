package de.miraculixx.mvanilla.data

enum class WhitelistType {
    GLOBAL,
    USER_RESTRICTED,
    PASSPHRASE_RESTRICTED;

    fun isRestricted(): Boolean {
        return this != GLOBAL
    }
}