package android.os

import java.io.Serializable
import java.util.concurrent.atomic.AtomicLong

object Build {
    const val VERSION_SDK_INT = 36
    object VERSION {
        const val SDK_INT = 36
        const val RELEASE = "16"

        @JvmStatic
        fun isAtLeast(minSdk: Int): Boolean = SDK_INT >= minSdk

        @JvmStatic
        fun isAtLeastO(): Boolean = SDK_INT >= 26
    }

    object VERSION_CODES {
        const val BASE = 1
        const val LOLLIPOP = 21
        const val M = 23
        const val N = 24
        const val N_MR1 = 25
        const val O = 26
        const val O_MR1 = 27
        const val P = 28
        const val Q = 29
        const val R = 30
        const val S = 31
        const val S_V2 = 32
        const val TIRAMISU = 33
        const val UPSIDE_DOWN_CAKE = 34
        const val VANILLA_ICE_CREAM = 35
        const val BAKLAVA = 36
    }
    const val MODEL = "Linux Desktop"
    const val MANUFACTURER = "RikkaHub-Linux"
    const val DEVICE = "desktop"
    const val BRAND = "RikkaHub"
    const val PRODUCT = "rikkahub"
    const val FINGERPRINT = "rikkahub/linux"
    const val HARDWARE = "desktop"
    const val BOARD = "desktop"
    const val DISPLAY = "rikkahub-linux"
    const val ID = "linux"
    const val TAGS = "release-keys"
    const val TYPE = "user"
    const val HOST = "localhost"
    const val USER = "rikkahub"
}

object SystemClock {
    private val bootOffset = AtomicLong(0)
    fun uptimeMillis(): Long = System.currentTimeMillis() - bootOffset.get()
    fun elapsedRealtime(): Long = uptimeMillis()
    fun elapsedRealtimeNanos(): Long = uptimeMillis() * 1_000_000
    fun currentThreadTimeMillis(): Long = System.nanoTime() / 1_000_000
    fun sleep(ms: Long) { Thread.sleep(ms) }
}

object Process {
    const val MY_UID = 10000
    const val MY_PID = 1
    fun myPid(): Int = ProcessHandle.current().pid().toInt()
    fun myUid(): Int = MY_UID
    fun killProcess(pid: Int) { Runtime.getRuntime().halt(0) }
    fun isThreadPriorityTid(tid: Int): Boolean = false
    fun setThreadPriority(priority: Int) {}
    fun setThreadPriority(tid: Int, priority: Int) {}
}

class Handler {
    constructor()
    constructor(looper: Looper)
    constructor(looper: Looper, callback: Callback?)

    fun interface Callback {
        fun handleMessage(msg: Message): Boolean
    }

    fun post(r: Runnable): Boolean { Thread(r).start(); return true }
    fun postDelayed(r: Runnable, delayMillis: Long): Boolean {
        Thread { Thread.sleep(delayMillis); r.run() }.start()
        return true
    }
    fun removeCallbacks(r: Runnable) {}
}

class Message {
    var what: Int = 0
    var arg1: Int = 0
    var arg2: Int = 0
    var obj: Any? = null
}

class Looper private constructor() {
    companion object {
        private val instance = Looper()
        fun myLooper(): Looper = instance
        fun getMainLooper(): Looper = instance
        fun myQueue(): Any = Any()
        fun prepare() {}
        fun loop() {}
    }
}

class CancellationSignal {
    private var canceled = false
    private val listeners = mutableListOf<() -> Unit>()
    val isCanceled: Boolean get() = canceled
    fun cancel() {
        canceled = true
        listeners.forEach { it() }
    }
    fun throwIfCanceled() { if (canceled) throw OperationCanceledException() }
    fun setOnCancelListener(listener: Runnable?) {
        if (listener != null) listeners.add { listener.run() }
    }
    class OperationCanceledException : Exception("Operation canceled")
}

class ParcelFileDescriptor private constructor(private val fd: Int) {
    fun close() {}
    companion object {
        fun fromFd(fd: Int): ParcelFileDescriptor = ParcelFileDescriptor(fd)
    }
}

interface IBinder

class Bundle(initialCapacity: Int = 0) {
    private val map = mutableMapOf<String, Any?>()
    constructor(bundle: Bundle) : this() { map.putAll(bundle.map) }
    fun putString(key: String, value: String?) { map[key] = value }
    fun getString(key: String): String? = map[key] as? String
    fun getString(key: String, def: String?): String = (map[key] as? String) ?: def ?: ""
    fun putInt(key: String, value: Int) { map[key] = value }
    fun getInt(key: String): Int = map[key] as? Int ?: 0
    fun putLong(key: String, value: Long) { map[key] = value }
    fun getLong(key: String): Long = map[key] as? Long ?: 0L
    fun putBoolean(key: String, value: Boolean) { map[key] = value }
    fun getBoolean(key: String): Boolean = map[key] as? Boolean ?: false
    fun putFloat(key: String, value: Float) { map[key] = value }
    fun getFloat(key: String): Float = map[key] as? Float ?: 0f
    fun putDouble(key: String, value: Double) { map[key] = value }
    fun getDouble(key: String): Double = map[key] as? Double ?: 0.0
    fun putStringArray(key: String, value: Array<String>?) { map[key] = value }
    fun getStringArray(key: String): Array<String>? = map[key] as? Array<String>
    fun putSerializable(key: String, value: Serializable?) { map[key] = value }
    fun getSerializable(key: String): Serializable? = map[key] as? Serializable
    fun putByteArray(key: String, value: ByteArray?) { map[key] = value }
    fun getByteArray(key: String): ByteArray? = map[key] as? ByteArray
    fun keySet(): Set<String> = map.keys
    fun containsKey(key: String): Boolean = map.containsKey(key)
    fun remove(key: String) { map.remove(key) }
    fun clear() { map.clear() }
    fun isEmpty(): Boolean = map.isEmpty()
    fun size(): Int = map.size
}

object Environment {
    val DIRECTORY_DOWNLOADS = "Downloads"
    val DIRECTORY_DOCUMENTS = "Documents"
    val DIRECTORY_PICTURES = "Pictures"
    val DIRECTORY_MUSIC = "Music"
    val DIRECTORY_MOVIES = "Movies"
    val DIRECTORY_DCIM = "DCIM"
    fun getExternalStorageDirectory(): java.io.File = java.io.File(System.getProperty("user.home")!!)
    fun getExternalStoragePublicDirectory(type: String): java.io.File =
        java.io.File(System.getProperty("user.home")!!, type)
    fun getDownloadCacheDirectory(): java.io.File = java.io.File(System.getProperty("java.io.tmpdir")!!)
    fun isExternalStorageEmulated(): Boolean = true
}

class BatteryManager {
    companion object {
    const val BATTERY_STATUS_DISCHARGING = 3
    const val BATTERY_STATUS_FULL = 5
    const val BATTERY_STATUS_CHARGING = 2
    const val BATTERY_PROPERTY_CAPACITY = 4
    const val BATTERY_PLUGGED_AC = 1
    const val BATTERY_PLUGGED_USB = 2
    const val EXTRA_STATUS = "status"
    const val EXTRA_LEVEL = "level"
    const val EXTRA_SCALE = "scale"
    const val EXTRA_PLUGGED = "plugged"
    const val BATTERY_PROPERTY_STATUS = 6
    const val BATTERY_PROPERTY_CHARGE_COUNTER = 1
    const val BATTERY_PROPERTY_CURRENT_NOW = 2
    const val BATTERY_PROPERTY_CURRENT_AVERAGE = 3
    const val BATTERY_PROPERTY_ENERGY_COUNTER = 5
    }

    fun getIntProperty(id: Int): Int = when (id) {
        BATTERY_PROPERTY_CAPACITY -> 100
        BATTERY_PROPERTY_STATUS -> BATTERY_STATUS_FULL
        else -> 0
    }

    fun getLongProperty(id: Int): Long = 0L
    fun isCharging(): Boolean = true
}

/** Desktop stand-in for android.os.LocaleList. */
class LocaleList(locales: Array<java.util.Locale>) {
    private val list: List<java.util.Locale> = locales.toList()

    fun size(): Int = list.size
    operator fun get(index: Int): java.util.Locale = list[index]
    fun isEmpty(): Boolean = list.isEmpty()
    fun toLanguageTags(): String = list.joinToString(",") { it.toLanguageTag() }
    override fun toString(): String = list.toString()

    companion object {
        private val default = LocaleList(arrayOf(java.util.Locale.getDefault()))

        @JvmStatic
        fun getDefault(): LocaleList = default

        @JvmStatic
        fun forLanguageTags(tags: String?): LocaleList {
            if (tags.isNullOrBlank()) return default
            return LocaleList(tags.split(",").map { java.util.Locale.forLanguageTag(it.trim()) }.toTypedArray())
        }
    }
}