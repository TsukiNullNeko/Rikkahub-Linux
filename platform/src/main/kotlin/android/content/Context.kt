package android.content

import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import java.io.File
import java.util.concurrent.ConcurrentHashMap

open class Context {
    val applicationContext: Context get() = this

    open val packageName: String get() = "me.rerere.rikkahub"

    open val filesDir: File
        get() = AppContext.dataDir.resolve("files").also { it.mkdirs() }

    open val cacheDir: File
        get() = AppContext.cacheDir.resolve("cache").also { it.mkdirs() }

    open val noBackupFilesDir: File get() = filesDir
    open val dataDir: File get() = AppContext.dataDir

    val mainLooper: android.os.Looper get() = android.os.Looper.getMainLooper()

    open val assets: AssetManager get() = AppContext.assets
    open val resources: Resources get() = AppContext.resources

    fun getString(id: Int): String = resources.getString(id)
    fun getString(id: Int, vararg args: Any?): String = resources.getString(id, *args)

    fun getColor(id: Int): Int = resources.getColor(id)

    open fun getExternalFilesDir(type: String?): File? {
        val base = AppContext.dataDir.resolve("external")
        return if (type == null) base else base.resolve(type)
    }

    open fun getExternalCacheDir(): File? = AppContext.cacheDir.resolve("external")

    open fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
        AppContext.getSharedPreferences(name)

    open fun getSystemService(name: String): Any? = AppContext.getSystemService(name)

    @Suppress("UNCHECKED_CAST")
    open fun <T : Any> getSystemService(serviceClass: Class<T>): T =
        (AppContext.getSystemService(serviceName(serviceClass)) as? T)
            ?: throw IllegalStateException("service not available: " + serviceClass.simpleName)

    private fun serviceName(cls: Class<*>): String = when (cls.simpleName) {
        "ClipboardManager" -> CLIPBOARD_SERVICE
        "NotificationManager" -> NOTIFICATION_SERVICE
        "ConnectivityManager" -> CONNECTIVITY_SERVICE
        "AudioManager" -> AUDIO_SERVICE
        "InputMethodManager" -> INPUT_METHOD_SERVICE
        "DownloadManager" -> DOWNLOAD_SERVICE
        "KeyguardManager" -> KEYGUARD_SERVICE
        "PowerManager" -> POWER_SERVICE
        else -> cls.simpleName
    }

    open fun startActivity(intent: Intent) {
        AppContext.startActivity(intent)
    }

    open fun startActivity(intent: Intent, options: android.os.Bundle?) {
        AppContext.startActivity(intent)
    }

    open fun sendBroadcast(intent: Intent) {}

    open fun startForegroundService(intent: Intent): Any? = null

    open fun bindService(intent: Intent, conn: Any?, flags: Int): Boolean = false

    open fun unbindService(conn: Any?) {}

    open fun startService(intent: Intent): Any? = null
    open fun stopService(intent: Intent): Boolean = true

    open fun checkSelfPermission(permission: String): Int = PackageManager.PERMISSION_GRANTED

    open fun checkPermission(permission: String, pid: Int, uid: Int): Int = PackageManager.PERMISSION_GRANTED

    open fun grantUriPermission(pkg: String?, uri: android.net.Uri?, flags: Int) {}

    open fun revokeUriPermission(uri: android.net.Uri?, flags: Int) {}

    open fun getDatabasePath(name: String): File = AppContext.dataDir.resolve("databases").resolve(name).also {
        it.parentFile?.mkdirs()
    }

    open fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase =
        SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory)

    open fun deleteDatabase(name: String): Boolean = getDatabasePath(name).delete()

    open fun getDir(name: String, mode: Int): File = AppContext.dataDir.resolve("app_" + name).also { it.mkdirs() }

    open fun fileList(): Array<String> =
        filesDir.list() ?: emptyArray()

    open fun deleteFile(name: String): Boolean = File(filesDir, name).delete()

    open fun openFileInput(name: String): java.io.FileInputStream = java.io.FileInputStream(File(filesDir, name))

    open fun openFileOutput(name: String, mode: Int): java.io.FileOutputStream =
        java.io.FileOutputStream(File(filesDir, name))

    open val contentResolver: ContentResolver get() = AppContext.contentResolver

    open val packageManager: PackageManager get() = AppContext.packageManager

    open val applicationInfo: android.app.ApplicationInfo
        get() = android.app.ApplicationInfo().apply {
            packageName = this@Context.packageName
            nativeLibraryDir = AppContext.dataDir.resolve("lib").absolutePath
            dataDir = AppContext.dataDir.absolutePath
        }

    companion object {
        const val CLIPBOARD_SERVICE = "clipboard"
        const val NOTIFICATION_SERVICE = "notification"
        const val CONNECTIVITY_SERVICE = "connectivity"
        const val WINDOW_SERVICE = "window"
        const val AUDIO_SERVICE = "audio"
        const val KEYGUARD_SERVICE = "keyguard"
        const val POWER_SERVICE = "power"
        const val VIBRATOR_SERVICE = "vibrator"
        const val DOWNLOAD_SERVICE = "download"
        const val INPUT_METHOD_SERVICE = "input_method"
        const val ACCESSIBILITY_SERVICE = "accessibility"
        const val USAGE_STATS_SERVICE = "usagestats"
        const val ACTIVITY_SERVICE = "activity"
        const val LOCATION_SERVICE = "location"
        const val SENSOR_SERVICE = "sensor"
        const val BATTERY_SERVICE = "batterymanager"
        const val MODE_PRIVATE = 0
        const val MODE_WORLD_READABLE = 1
        const val MODE_WORLD_WRITEABLE = 2
        const val MODE_APPEND = 32768
    }

    // Desktop-only extension: launch a URI in the system browser / default app
    open fun openUri(uri: android.net.Uri): Boolean {
        return try {
            val pb = ProcessBuilder("xdg-open", uri.toString())
            pb.start()
            true
        } catch (e: Exception) {
            android.util.Log.e("Context", "openUri failed", e)
            false
        }
    }
}