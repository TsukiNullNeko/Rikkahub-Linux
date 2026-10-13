package me.rerere.ui.components

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.roundToInt

private val MAX_HEIGHT = 10000.dp
private val MAX_WIDTH = 10000.dp

val LocalExportContext = staticCompositionLocalOf { false }

/**
 * Draws an arbitrary composable into a bitmap.
 *
 * Desktop implementation: renders the composable offscreen through Compose's
 * [ImageComposeScene] (Skia) instead of attaching an Android view to the window.
 */
class BitmapComposer(private val mainScope: CoroutineScope) {
    suspend fun composableToBitmap(
        activity: Activity,
        width: Dp? = null,
        height: Dp? = null,
        screenDensity: Density,
        content: @Composable () -> Unit
    ): Bitmap = withContext(Dispatchers.Main) {
        val contentWidthInPixels = (screenDensity.density * (width ?: MAX_WIDTH).value).roundToInt().coerceAtLeast(1)
        val contentHeightInPixels = (screenDensity.density * (height ?: MAX_HEIGHT).value).roundToInt().coerceAtLeast(1)

        val scene = ImageComposeScene(
            width = contentWidthInPixels,
            height = contentHeightInPixels,
            density = screenDensity
        ) {
            CompositionLocalProvider(LocalExportContext provides true) {
                content()
            }
        }

        try {
            // Render twice: first pass lays out async content, second pass captures the settled frame.
            scene.render()
            val image = scene.render()
            val pngBytes = image.encodeToData()?.bytes ?: error("failed to encode rendered bitmap")
            val buffered = ImageIO.read(ByteArrayInputStream(pngBytes)) ?: error("failed to decode rendered bitmap")
            val cropped = if (height == null) cropTransparentBottom(buffered) else buffered
            Bitmap(cropped)
        } finally {
            scene.close()
        }
    }

    /** Crops fully transparent rows at the bottom, mimicking Android's AT_MOST measurement. */
    private fun cropTransparentBottom(image: BufferedImage): BufferedImage {
        var lastContentRow = image.height - 1
        while (lastContentRow > 0) {
            var hasContent = false
            for (x in 0 until image.width) {
                val alpha = (image.getRGB(x, lastContentRow) ushr 24) and 0xFF
                if (alpha != 0) {
                    hasContent = true
                    break
                }
            }
            if (hasContent) break
            lastContentRow--
        }
        if (lastContentRow >= image.height - 1) return image
        val croppedHeight = (lastContentRow + 1).coerceAtLeast(1)
        val out = BufferedImage(image.width, croppedHeight, BufferedImage.TYPE_INT_ARGB)
        val g = out.createGraphics()
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return out
    }
}
