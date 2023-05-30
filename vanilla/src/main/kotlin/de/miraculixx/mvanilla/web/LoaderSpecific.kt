package de.miraculixx.mvanilla.web

import java.util.UUID

abstract class LoaderSpecific {
    companion object {
        var INSTANCE: LoaderSpecific? = null
    }

    abstract fun uuidToPlayerName(uuid: UUID): String
}