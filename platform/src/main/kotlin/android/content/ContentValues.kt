package android.content

import android.net.Uri

class ContentValues(initialCapacity: Int = 8) {
    private val map = LinkedHashMap<String, Any?>()

    constructor(values: ContentValues) : this() {
        map.putAll(values.map)
    }

    fun put(key: String, value: String?) { map[key] = value }
    fun put(key: String, value: Int?) { map[key] = value }
    fun put(key: String, value: Long?) { map[key] = value }
    fun put(key: String, value: Float?) { map[key] = value }
    fun put(key: String, value: Double?) { map[key] = value }
    fun put(key: String, value: Boolean?) { map[key] = value }
    fun put(key: String, value: ByteArray?) { map[key] = value }
    fun putNull(key: String) { map[key] = null }
    fun get(key: String): Any? = map[key]
    fun getAsString(key: String): String? = map[key]?.toString()
    fun getAsLong(key: String): Long? = (map[key] as? Number)?.toLong()
    fun getAsInt(key: String): Int? = (map[key] as? Number)?.toInt()
    fun getAsBoolean(key: String): Boolean? = map[key] as? Boolean
    fun containsKey(key: String): Boolean = map.containsKey(key)
    fun remove(key: String) { map.remove(key) }
    fun clear() { map.clear() }
    fun size(): Int = map.size
    fun keySet(): Set<String> = map.keys
    fun valueSet(): Collection<Any?> = map.values
    fun isEmpty(): Boolean = map.isEmpty()
    fun putAll(values: ContentValues) { map.putAll(values.map) }
}

object ContentUris {
    fun withAppendedId(uri: Uri, id: Long): Uri = Uri.parse(uri.toString() + "/" + id)
    fun parseId(uri: Uri): Long = uri.lastPathSegment?.toLongOrNull() ?: -1L
    fun appendId(builder: Uri.Builder, id: Long): Uri.Builder = builder.appendPath(id.toString())
}
