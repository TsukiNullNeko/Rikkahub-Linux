package androidx.compose.ui.res

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import org.xml.sax.InputSource

/**
 * Maps Android resource ids to bundled resource paths (populated by the app at startup
 * from the generated RDrawableTable).
 */
object DrawableResourceRegistry {
    @Volatile
    var byId: Map<Int, String> = emptyMap()
}

/**
 * Desktop implementation of painterResource(@DrawableRes id: Int).
 * Supports PNG/JPEG bitmaps, Android XML vector drawables and SVG files.
 */
@Composable
fun painterResource(id: Int): Painter {
    val density = LocalDensity.current
    val path = DrawableResourceRegistry.byId[id]
    if (path == null) {
        return remember { ColorPainter(Color.Transparent) }
    }

    return when {
        path.endsWith(".xml", ignoreCase = true) -> {
            val vector = remember(id) { loadXmlVector(path, density) }
            if (vector != null) {
                rememberVectorPainter(vector)
            } else {
                remember { ColorPainter(Color.Transparent) }
            }
        }

        path.endsWith(".svg", ignoreCase = true) -> {
            remember(id) { loadSvg(path, density) } ?: remember { ColorPainter(Color.Transparent) }
        }

        else -> {
            remember(id) { loadBitmap(path) } ?: remember { ColorPainter(Color.Transparent) }
        }
    }
}

private fun openResource(path: String): java.io.InputStream? =
    Thread.currentThread().contextClassLoader?.getResourceAsStream(path)
        ?: object {}.javaClass.getResourceAsStream("/" + path)

private fun loadXmlVector(path: String, density: Density): ImageVector? = runCatching {
    val bytes = openResource(path)?.use { it.readBytes() } ?: return null
    loadXmlImageVector(InputSource(bytes.inputStream()), density)
}.getOrNull()

private fun loadSvg(path: String, density: Density): Painter? = runCatching {
    val stream = openResource(path) ?: return null
    stream.use { loadSvgPainter(it, density) }
}.getOrNull()

private fun loadBitmap(path: String): Painter? = runCatching {
    val stream = openResource(path) ?: return null
    stream.use { BitmapPainter(loadImageBitmap(it)) }
}.getOrNull()
