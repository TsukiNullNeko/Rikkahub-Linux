package me.rerere.rikkahub.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.LoadedFont

/**
 * Loads a bundled TTF from the application classpath (/font/<name>.ttf) as a Compose font.
 * Replaces the Android resource-based Font(R.font.xxx) constructor on desktop.
 */
@OptIn(ExperimentalTextApi::class)
fun desktopFont(
    name: String,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    variationSettings: FontVariation.Settings? = null
): Font = LoadedFont(
    identity = name,
    getData = { loadFontBytes(name) },
    weight = weight,
    style = style,
    variationSettings = variationSettings ?: FontVariation.Settings()
)

private val fontCache = HashMap<String, ByteArray>()

private fun loadFontBytes(name: String): ByteArray = synchronized(fontCache) {
    fontCache.getOrPut(name) {
        val stream = Thread.currentThread().contextClassLoader?.getResourceAsStream("font/$name.ttf")
            ?: object {}.javaClass.getResourceAsStream("/font/$name.ttf")
            ?: error("bundled font not found: font/$name.ttf")
        stream.use { it.readBytes() }
    }
}