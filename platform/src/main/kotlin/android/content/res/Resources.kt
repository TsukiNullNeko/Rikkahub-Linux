package android.content.res

import java.io.File
import android.content.AppContext

/**
 * Holds string resources loaded from generated string tables.
 * The desktop app populates [stringsById] at startup from build-time generated tables.
 */
class Resources {
    companion object {
        /** id -> text for the current locale; populated by the app at startup. */
        @Volatile
        var stringsById: Map<Int, String> = emptyMap()

        /** id -> raw string (with %1$s placeholders unresolved). */
        @Volatile
        var formattedStringsById: Map<Int, String> = emptyMap()
    }

    val displayMetrics: Any get() = Any()
    val configuration: Configuration get() = Configuration()

    val assets: AssetManager get() = AppContext.assets

    fun getString(id: Int): String =
        stringsById[id] ?: formattedStringsById[id] ?: ("[missing string 0x" + Integer.toHexString(id) + "]")

    fun getString(id: Int, vararg args: Any?): String {
        val template = formattedStringsById[id] ?: return getString(id)
        var out = template
        // Android uses %1$s style; convert and replace positionally.
        args.forEachIndexed { index, arg ->
            out = out.replace("%" + (index + 1) + "\$s", arg?.toString() ?: "")
            out = out.replace("%" + (index + 1) + "\$d", arg?.toString() ?: "")
        }
        // Also replace bare %s / %d sequentially.
        var argIndex = 0
        out = out.replace(Regex("%s|%d")) {
            val v = args.getOrNull(argIndex)?.toString() ?: ""
            argIndex++
            v
        }
        return out
    }

    fun getQuantityString(id: Int, quantity: Int, vararg args: Any?): String = getString(id, *args)

    fun getIdentifier(name: String, defType: String?, defPackage: String?): Int = 0

    fun getColor(id: Int): Int = 0

    fun getDimension(id: Int): Float = 0f

    fun getFloat(id: Int): Float = 0f

    fun openRawResource(id: Int): java.io.InputStream =
        throw UnsupportedOperationException("raw resources are not supported on desktop")
}

class Configuration {
    var locale: java.util.Locale = java.util.Locale.getDefault()
    var locales: android.os.LocaleList = android.os.LocaleList.getDefault()
        get() = field
    var fontScale: Float = 1f
    var densityDpi: Int = 160
    var orientation: Int = 1
    var screenWidthDp: Int = 1280
    var screenHeightDp: Int = 800
    var uiMode: Int = 0x11
    var smallestScreenWidthDp: Int = 800
}

class AssetManager private constructor(private val roots: List<File>) {
    fun list(path: String): Array<String> {
        return roots.mapNotNull { root ->
            val dir = root.resolve(path)
            if (dir.isDirectory) dir.list() else null
        }.flatMap { it.toList() }.distinct().toTypedArray()
    }

    fun open(fileName: String): java.io.InputStream {
        for (root in roots) {
            val f = root.resolve(fileName)
            if (f.exists()) return f.inputStream()
        }
        throw java.io.FileNotFoundException(fileName)
    }

    companion object {
        /**
         * Looks for an "assets" directory next to the application or in the working tree.
         * When running from the packaged app, assets live in <install>/assets.
         */
        fun defaultAssets(): AssetManager {
            val candidates = mutableListOf<File>()
            val prop = System.getProperty("rikkahub.assets")
            if (prop != null) candidates.add(File(prop))
            candidates.add(File("assets"))
            val appDir = System.getProperty("rikkahub.home")
            if (appDir != null) candidates.add(File(appDir, "assets"))
            candidates.add(File("app/src/main/assets"))
            return AssetManager(candidates.filter { it.isDirectory })
        }
    }
}