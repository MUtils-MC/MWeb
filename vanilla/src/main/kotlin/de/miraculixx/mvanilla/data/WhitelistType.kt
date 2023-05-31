package de.miraculixx.mvanilla.data

enum class WhitelistType(val head: Head64) {
    GLOBAL(Head64.GLOBE),
    USER_RESTRICTED(Head64.ENDER_CHEST),
    PASSPHRASE_RESTRICTED(Head64.KEY_HOLE);

    fun isRestricted(): Boolean {
        return this != GLOBAL
    }
}