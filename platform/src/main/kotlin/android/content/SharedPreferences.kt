package android.content

import java.io.File
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

interface SharedPreferences {
    fun getString(key: String?, defValue: String?): String?
    fun getStringSet(key: String?, defValues: Set<String>?): Set<String>?
    fun getInt(key: String?, defValue: Int): Int
    fun getLong(key: String?, defValue: Long): Long
    fun getFloat(key: String?, defValue: Float): Float
    fun getBoolean(key: String?, defValue: Boolean): Boolean
    fun contains(key: String?): Boolean
    fun edit(): Editor
    fun getAll(): Map<String, *>
    fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?)
    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?)

    fun interface OnSharedPreferenceChangeListener {
        fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?)
    }

    interface Editor {
        fun putString(key: String?, value: String?): Editor
        fun putStringSet(key: String?, values: Set<String>?): Editor
        fun putInt(key: String?, value: Int): Editor
        fun putLong(key: String?, value: Long): Editor
        fun putFloat(key: String?, value: Float): Editor
        fun putBoolean(key: String?, value: Boolean): Editor
        fun remove(key: String?): Editor
        fun clear(): Editor
        fun apply()
        fun commit(): Boolean
    }
}

class SharedPreferencesImpl(private val file: File) : SharedPreferences {
    private val values = ConcurrentHashMap<String, Any>()

    init {
        try {
            if (file.exists()) {
                val props = Properties()
                file.inputStream().use { props.load(it) }
                for ((k, v) in props) values[k.toString()] = v.toString()
            }
        } catch (e: Exception) {
            android.util.Log.e("SharedPreferences", "failed to load " + file, e)
        }
    }

    private fun persist() {
        try {
            file.parentFile?.mkdirs()
            val props = Properties()
            for ((k, v) in values) props.setProperty(k, v.toString())
            file.outputStream().use { props.store(it, null) }
        } catch (e: Exception) {
            android.util.Log.e("SharedPreferences", "failed to persist " + file, e)
        }
    }

    override fun getString(key: String?, defValue: String?): String? = values[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? {
        val v = values[key] as? String ?: return defValues
        return v.split("\u0001").filter { it.isNotEmpty() }.toSet()
    }
    override fun getInt(key: String?, defValue: Int): Int = (values[key] as? String)?.toIntOrNull() ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = (values[key] as? String)?.toLongOrNull() ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = (values[key] as? String)?.toFloatOrNull() ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        (values[key] as? String)?.let { it == "true" } ?: defValue
    override fun contains(key: String?): Boolean = key != null && values.containsKey(key)
    override fun getAll(): Map<String, *> = values.toMap()

    override fun edit(): SharedPreferences.Editor = EditorImpl()

    private val listeners = CopyOnWriteArrayList<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        if (listener != null) listeners.add(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        listeners.remove(listener)
    }

    private fun notifyListeners(keys: Collection<String>) {
        keys.forEach { key -> listeners.forEach { it.onSharedPreferenceChanged(this, key) } }
    }

    private inner class EditorImpl : SharedPreferences.Editor {
        private val pending = LinkedHashMap<String, Any?>()
        private val removed = mutableSetOf<String>()
        private var clearAll = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor { pending[key!!] = value ?: ""; return this }
        override fun putStringSet(key: String?, values: Set<String>?): SharedPreferences.Editor {
            pending[key!!] = values?.joinToString("\u0001") ?: ""; return this
        }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor { pending[key!!] = value.toString(); return this }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor { pending[key!!] = value.toString(); return this }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { pending[key!!] = value.toString(); return this }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { pending[key!!] = value.toString(); return this }
        override fun remove(key: String?): SharedPreferences.Editor { removed.add(key!!); return this }
        override fun clear(): SharedPreferences.Editor { clearAll = true; return this }
        override fun apply() { commit() }
        override fun commit(): Boolean {
            if (clearAll) values.clear()
            removed.forEach { values.remove(it) }
            for ((k, v) in pending) values[k] = v ?: ""
            persist()
            notifyListeners(pending.keys + removed)
            return true
        }
    }
}