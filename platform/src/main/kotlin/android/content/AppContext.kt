package android.content

import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.ConnectivityManager
import java.io.File

/**
 * Desktop implementation of the application Context.
 *
 * Data locations follow XDG conventions:
 *  - data   : ~/.local/share/rikkahub
 *  - cache  : ~/.cache/rikkahub
 *  - config : ~/.config/rikkahub
 *
 * Override with -Drikkahub.data=/path for portable/test installs.
 */
object AppContext {
    private val home: File = File(System.getProperty("user.home")!!)

    val dataDir: File = File(
        System.getProperty("rikkahub.data")
            ?: System.getenv("RIKKAHUB_DATA")
            ?: (System.getenv("XDG_DATA_HOME") ?: home.resolve(".local/share").absolutePath),
        "rikkahub"
    )

    val cacheDir: File = File(
        System.getProperty("rikkahub.cache")
            ?: (System.getenv("XDG_CACHE_HOME") ?: home.resolve(".cache").absolutePath),
        "rikkahub"
    )

    val configDir: File = File(
        System.getProperty("rikkahub.config")
            ?: (System.getenv("XDG_CONFIG_HOME") ?: home.resolve(".config").absolutePath),
        "rikkahub"
    )

    val downloadsDir: File get() = EnvironmentHelper.downloads()

    private val prefs = HashMap<String, SharedPreferencesImpl>()

    val assets: AssetManager by lazy { AssetManager.defaultAssets() }

    val resources: Resources by lazy { Resources() }

    val contentResolver: ContentResolver by lazy { ContentResolver() }

    val packageManager: PackageManager by lazy { PackageManager() }

    @Volatile
    var toastHandler: ((ToastData) -> Unit)? = null

    data class ToastData(val message: String, val duration: Int)

    @Volatile
    var activityHandler: ((Intent) -> Unit)? = null

    init {
        dataDir.mkdirs()
        cacheDir.mkdirs()
        configDir.mkdirs()
    }

    @Synchronized
    fun getSharedPreferences(name: String): SharedPreferencesImpl {
        return prefs.getOrPut(name) {
            SharedPreferencesImpl(configDir.resolve("shared_prefs").resolve("$name.xml"))
        }
    }

    fun getSystemService(name: String): Any? = when (name) {
        Context.CLIPBOARD_SERVICE -> clipboard
        Context.NOTIFICATION_SERVICE -> notificationManager
        Context.CONNECTIVITY_SERVICE -> connectivityManager
        Context.KEYGUARD_SERVICE -> android.app.KeyguardManager()
        Context.POWER_SERVICE -> android.app.PowerManager()
        Context.USAGE_STATS_SERVICE -> android.app.usage.UsageStatsManager()
        else -> null
    }

    /** The shared desktop Context instance. */
    val appContext: Context by lazy { Context() }

    val clipboard by lazy { ClipboardManager() }
    val notificationManager by lazy { android.app.NotificationManager() }
    val connectivityManager by lazy { ConnectivityManager() }

    fun startActivity(intent: Intent) {
        val handler = activityHandler
        if (handler != null) {
            handler(intent)
            return
        }
        val uri = intent.data
        if (uri != null && intent.action == Intent.ACTION_VIEW) {
            openExternally(uri)
        }
    }

    fun openExternally(uri: android.net.Uri): Boolean {
        return try {
            ProcessBuilder("xdg-open", uri.toString()).start()
            true
        } catch (e: Exception) {
            android.util.Log.e("AppContext", "xdg-open failed for " + uri, e)
            false
        }
    }

    private object EnvironmentHelper {
        fun downloads(): File {
            val xdg = System.getenv("XDG_DOWNLOADS_DIR")
            if (xdg != null) return File(xdg)
            val config = System.getenv("XDG_CONFIG_HOME") ?: home.resolve(".config").absolutePath
            val dirsFile = File(config, "user-dirs.dirs")
            if (dirsFile.exists()) {
                try {
                    val line = dirsFile.readLines().firstOrNull { it.contains("XDG_DOWNLOAD_DIR") }
                    if (line != null) {
                        val path = line.substringAfter("\$HOME", "").removePrefix("/").trim('/', '"')
                        if (path.isNotBlank()) return home.resolve(path)
                    }
                } catch (_: Exception) {}
            }
            return home.resolve("Downloads")
        }
    }
}