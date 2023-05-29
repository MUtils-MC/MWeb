package de.miraculixx.mvanilla.serializer

fun <K, V>Map<K, V>.add(map: Map<K, V>): Map<K, V> {
    return buildMap {
        putAll(this@add)
        putAll(map)
    }
}

fun <K, V>List<K>.toMap(default: V): Map<K, V> {
    return buildMap {
        this@toMap.forEach { put(it, default) }
    }
}

inline fun <reified T : Enum<T>> enumOf(type: String?): T? {
    if (type == null) return null
    return try {
        java.lang.Enum.valueOf(T::class.java, type)
    } catch (e: IllegalArgumentException) {
        null
    }
}