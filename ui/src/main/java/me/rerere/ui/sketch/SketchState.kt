package me.rerere.ui.sketch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.awt.BasicStroke
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/** Result of a sketch session, encoded as PNG bytes. */
class SketchResult(private val pngBytes: ByteArray) {
    val extension: String get() = "png"
    val mimeType: String get() = "image/png"

    fun encode(): ByteArray = pngBytes

    fun toBufferedImage(): BufferedImage? = ImageIO.read(pngBytes.inputStream())
}

internal data class SketchStroke(
    val points: List<Offset>,
    val color: Color,
    val width: Float
)

internal object SketchDefaults {
    val colors = listOf(
        Color(0xFF141B34),
        Color(0xFFE53935),
        Color(0xFF1E88E5),
        Color(0xFF43A047),
        Color(0xFFFDD835),
        Color(0xFF8E24AA),
        Color(0xFFFFFFFF),
    )
    const val DEFAULT_WIDTH = 4f
    val widths = listOf(2f, 4f, 8f, 16f)
}

/** Desktop sketch state: freehand strokes with undo/redo, background image and paper ratio. */
internal class SketchState(aspectRatio: Float? = null) {
    val strokes = mutableStateListOf<SketchStroke>()
    private val undone = mutableStateListOf<SketchStroke>()

    var color by mutableStateOf(SketchDefaults.colors.first())
    var width by mutableStateOf(SketchDefaults.DEFAULT_WIDTH)
    var erasing by mutableStateOf(false)
    var background by mutableStateOf<BufferedImage?>(null)
    var aspectRatio by mutableStateOf(aspectRatio)

    val isBlank: Boolean get() = strokes.isEmpty()
    val canUndo: Boolean get() = strokes.isNotEmpty()
    val canRedo: Boolean get() = undone.isNotEmpty()

    fun beginStroke(point: Offset) {
        undone.clear()
        strokes.add(
            SketchStroke(
                points = listOf(point),
                color = if (erasing) Color.Transparent else color,
                width = if (erasing) width * 2.5f else width
            )
        )
    }

    fun extendStroke(point: Offset) {
        val last = strokes.lastOrNull() ?: return
        strokes[strokes.size - 1] = last.copy(points = last.points + point)
    }

    fun undo() {
        if (strokes.isNotEmpty()) undone.add(strokes.removeAt(strokes.size - 1))
    }

    fun redo() {
        if (undone.isNotEmpty()) strokes.add(undone.removeAt(undone.size - 1))
    }

    fun clear() {
        strokes.clear()
        undone.clear()
    }

    /** Rasterizes background + strokes into PNG bytes. */
    fun encode(widthPx: Int, heightPx: Int): ByteArray {
        val image = BufferedImage(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = java.awt.Color.WHITE
        g.fillRect(0, 0, image.width, image.height)
        background?.let { g.drawImage(it, 0, 0, image.width, image.height, null) }

        for (stroke in strokes) {
            if (stroke.points.isEmpty()) continue
            val awtColor = java.awt.Color(stroke.color.toArgb(), true)
            if (stroke.points.size == 1) {
                val p = stroke.points.first()
                g.color = awtColor
                val d = stroke.width.toInt().coerceAtLeast(1)
                g.fillOval((p.x - d / 2f).toInt(), (p.y - d / 2f).toInt(), d, d)
                continue
            }
            g.color = awtColor
            g.stroke = BasicStroke(stroke.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            for (i in 0 until stroke.points.size - 1) {
                val a = stroke.points[i]
                val b = stroke.points[i + 1]
                g.drawLine(a.x.toInt(), a.y.toInt(), b.x.toInt(), b.y.toInt())
            }
        }
        g.dispose()
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        return out.toByteArray()
    }
}