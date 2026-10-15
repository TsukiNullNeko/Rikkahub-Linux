package android.graphics

import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage
import java.io.OutputStream
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.sin

/**
 * Desktop Bitmap backed by java.awt.image.BufferedImage.
 */
class Bitmap(val image: BufferedImage) {
    val width: Int get() = image.width
    val height: Int get() = image.height
    val density: Int get() = 160
    fun hasAlpha(): Boolean = image.colorModel.hasAlpha()
    val isMutable: Boolean get() = image.isAlphaPremultiplied || true
    val config: Config get() = if (hasAlpha()) Config.ARGB_8888 else Config.RGB_565
    val byteCount: Int get() = width * height * 4

    enum class Config { ARGB_8888, RGB_565, ALPHA_8, HARDWARE }
    enum class CompressFormat { JPEG, PNG, WEBP }

    fun compress(format: CompressFormat, quality: Int, stream: OutputStream): Boolean {
        return try {
            val fmt = when (format) {
                CompressFormat.PNG -> "png"
                CompressFormat.JPEG -> "jpg"
                CompressFormat.WEBP -> "png"
            }
            if (fmt == "jpg" && image.colorModel.hasAlpha()) {
                val rgb = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                val g = rgb.createGraphics()
                g.drawImage(image, 0, 0, java.awt.Color.WHITE, null)
                g.dispose()
                ImageIO.write(rgb, fmt, stream)
            } else {
                ImageIO.write(image, fmt, stream)
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("Bitmap", "compress failed", e)
            false
        }
    }

    fun recycle() {}
    fun isRecycled(): Boolean = false

    fun getPixels(pixels: IntArray, offset: Int, stride: Int, x: Int, y: Int, w: Int, h: Int) {
        var idx = offset
        for (row in 0 until h) {
            for (col in 0 until w) {
                pixels[idx++] = getPixel(x + col, y + row)
            }
            idx += (stride - w)
        }
    }

    fun getPixel(x: Int, y: Int): Int {
        if (x < 0 || y < 0 || x >= width || y >= height) return 0
        return image.getRGB(x, y)
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        if (x < 0 || y < 0 || x >= width || y >= height) return
        image.setRGB(x, y, color)
    }

    fun copy(config: Config, isMutable: Boolean): Bitmap {
        val out = BufferedImage(width, height, if (config == Config.RGB_565) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB)
        val g = out.createGraphics()
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return Bitmap(out)
    }

    fun asImageBitmap(): androidx.compose.ui.graphics.ImageBitmap =
        image.toComposeImageBitmap()

    companion object {
        fun createBitmap(width: Int, height: Int, config: Config = Config.ARGB_8888): Bitmap =
            Bitmap(BufferedImage(width.coerceAtLeast(1), height.coerceAtLeast(1), if (config == Config.RGB_565) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB))

        fun createBitmap(src: Bitmap, x: Int, y: Int, width: Int, height: Int, m: Matrix?, filter: Boolean): Bitmap {
            val out = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g = out.createGraphics()
            g.drawImage(src.image, -x, -y, null)
            g.dispose()
            val result = Bitmap(out)
            return if (m == null) result else m.applyTo(result)
        }

        fun createBitmap(colors: IntArray, width: Int, height: Int, config: Config): Bitmap {
            val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            var idx = 0
            for (y in 0 until height) for (x in 0 until width) {
                if (idx < colors.size) img.setRGB(x, y, colors[idx])
                idx++
            }
            return Bitmap(img)
        }

        fun createBitmap(source: Bitmap, width: Int, height: Int, config: Config, hasAlpha: Boolean = true): Bitmap =
            createScaledBitmap(source, width, height, true)

        fun createScaledBitmap(src: Bitmap, dstWidth: Int, dstHeight: Int, filter: Boolean): Bitmap {
            val out = BufferedImage(dstWidth.coerceAtLeast(1), dstHeight.coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB)
            val g = out.createGraphics()
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                if (filter) java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR else java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
            g.drawImage(src.image, 0, 0, dstWidth, dstHeight, null)
            g.dispose()
            return Bitmap(out)
        }
    }
}

class BitmapFactory {
    class Options {
        var inSampleSize: Int = 1
        var inJustDecodeBounds: Boolean = false
        var inPreferredConfig: Bitmap.Config = Bitmap.Config.ARGB_8888
        var inMutable: Boolean = false
        var inScaled: Boolean = true
        var outWidth: Int = -1
        var outHeight: Int = -1
        var outMimeType: String? = null
    }

    companion object {
        fun decodeFile(pathName: String): Bitmap = decodeFile(pathName, null)

        fun decodeFile(pathName: String, opts: Options?): Bitmap {
            return try {
                val file = java.io.File(pathName)
                if (!file.exists()) return Bitmap.createBitmap(1, 1)
                val img = ImageIO.read(file) ?: return Bitmap.createBitmap(1, 1)
                opts?.let { it.outWidth = img.width; it.outHeight = img.height }
                if (opts?.inJustDecodeBounds == true) return Bitmap.createBitmap(1, 1)
                Bitmap(img)
            } catch (e: Exception) {
                android.util.Log.e("BitmapFactory", "decodeFile failed", e)
                Bitmap.createBitmap(1, 1)
            }
        }

        fun decodeStream(input: java.io.InputStream?): Bitmap = decodeStream(input, null)

        fun decodeStream(input: java.io.InputStream?, outPadding: android.graphics.Rect?, opts: Options?): Bitmap =
            decodeStream(input, opts)

        fun decodeFile(pathName: String, outPadding: android.graphics.Rect?, opts: Options?): Bitmap =
            decodeFile(pathName, opts)

        fun decodeByteArray(data: ByteArray, offset: Int, length: Int, opts: Options?, outPadding: android.graphics.Rect?): Bitmap =
            decodeByteArray(data, offset, length, opts)

        fun decodeStream(input: java.io.InputStream?, opts: Options?): Bitmap {
            if (input == null) return Bitmap.createBitmap(1, 1)
            return try {
                val img = ImageIO.read(input) ?: return Bitmap.createBitmap(1, 1)
                opts?.let { it.outWidth = img.width; it.outHeight = img.height }
                Bitmap(img)
            } catch (e: Exception) {
                Bitmap.createBitmap(1, 1)
            }
        }

        fun decodeByteArray(data: ByteArray, offset: Int, length: Int): Bitmap = decodeByteArray(data, offset, length, null)

        fun decodeByteArray(data: ByteArray, offset: Int, length: Int, opts: Options?): Bitmap {
            return try {
                val img = ImageIO.read(java.io.ByteArrayInputStream(data, offset, length)) ?: return Bitmap.createBitmap(1, 1)
                opts?.let { it.outWidth = img.width; it.outHeight = img.height }
                Bitmap(img)
            } catch (e: Exception) {
                Bitmap.createBitmap(1, 1)
            }
        }

        fun decodeResource(res: Any?, id: Int): Bitmap? = null
    }
}

class Matrix {
    private val ops = mutableListOf<(BufferedImage) -> BufferedImage>()

    fun reset() { ops.clear() }
    fun isIdentity(): Boolean = ops.isEmpty()

    fun setRotate(degrees: Float) { ops.clear(); ops.add { rotate(it, degrees) } }
    fun postRotate(degrees: Float) { ops.add { rotate(it, degrees) } }
    fun preRotate(degrees: Float) { ops.add(0) { rotate(it, degrees) } }

    fun setScale(sx: Float, sy: Float) { ops.clear(); ops.add { scale(it, sx, sy) } }
    fun postScale(sx: Float, sy: Float) { ops.add { scale(it, sx, sy) } }
    fun preScale(sx: Float, sy: Float) { ops.add(0) { scale(it, sx, sy) } }

    fun setTranslate(dx: Float, dy: Float) { ops.clear(); ops.add { translate(it, dx, dy) } }
    fun postTranslate(dx: Float, dy: Float) { ops.add { translate(it, dx, dy) } }
    fun preTranslate(dx: Float, dy: Float) { ops.add(0) { translate(it, dx, dy) } }

    fun setSkew(kx: Float, ky: Float) {}
    fun postConcat(other: Matrix) { ops.addAll(other.ops) }

    fun mapPoints(pts: FloatArray) {}

    internal fun applyTo(src: Bitmap): Bitmap {
        var img = src.image
        ops.forEach { img = it(img) }
        return Bitmap(img)
    }

    private fun rotate(src: BufferedImage, degrees: Float): BufferedImage {
        val rad = Math.toRadians(degrees.toDouble())
        val sin = kotlin.math.abs(sin(rad))
        val cos = kotlin.math.abs(cos(rad))
        val w = (src.width * cos + src.height * sin).toInt()
        val h = (src.width * sin + src.height * cos).toInt()
        val out = BufferedImage(w.coerceAtLeast(1), h.coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB)
        val g = out.createGraphics()
        g.rotate(rad, w / 2.0, h / 2.0)
        g.drawImage(src, (w - src.width) / 2, (h - src.height) / 2, null)
        g.dispose()
        return out
    }

    private fun scale(src: BufferedImage, sx: Float, sy: Float): BufferedImage {
        val w = (src.width * sx).toInt().coerceAtLeast(1)
        val h = (src.height * sy).toInt().coerceAtLeast(1)
        val out = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g = out.createGraphics()
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.drawImage(src, 0, 0, w, h, null)
        g.dispose()
        return out
    }

    private fun translate(src: BufferedImage, dx: Float, dy: Float): BufferedImage {
        val out = BufferedImage(src.width, src.height, BufferedImage.TYPE_INT_ARGB)
        val g = out.createGraphics()
        g.drawImage(src, dx.toInt(), dy.toInt(), null)
        g.dispose()
        return out
    }
}

class Rect {
    var left: Int = 0
    var top: Int = 0
    var right: Int = 0
    var bottom: Int = 0

    constructor()
    constructor(left: Int, top: Int, right: Int, bottom: Int) {
        this.left = left; this.top = top; this.right = right; this.bottom = bottom
    }

    fun width(): Int = right - left
    fun height(): Int = bottom - top
    fun centerX(): Int = (left + right) / 2
    fun centerY(): Int = (top + bottom) / 2
    fun isEmpty(): Boolean = left >= right || top >= bottom
    fun set(left: Int, top: Int, right: Int, bottom: Int) {
        this.left = left; this.top = top; this.right = right; this.bottom = bottom
    }
    fun setEmpty() { left = 0; top = 0; right = 0; bottom = 0 }
    fun offset(dx: Int, dy: Int) { left += dx; right += dx; top += dy; bottom += dy }
    fun inset(dx: Int, dy: Int) { left += dx; top += dy; right -= dx; bottom -= dy }
    fun contains(x: Int, y: Int): Boolean = x >= left && x < right && y >= top && y < bottom
}

object Color {
    const val BLACK = -0x1000000
    const val WHITE = -0x1
    const val RED = -0x10000
    const val GREEN = -0xff0100
    const val BLUE = -0xffff01
    const val TRANSPARENT = 0
    const val YELLOW = -0x100
    const val CYAN = -0xff0001
    const val MAGENTA = -0xff01
    const val GRAY = -0x7f7f80
    const val LTGRAY = -0x333334
    const val DKGRAY = -0xbbbbbc

    fun argb(a: Int, r: Int, g: Int, b: Int): Int = (a shl 24) or (r shl 16) or (g shl 8) or b
    fun rgb(r: Int, g: Int, b: Int): Int = argb(255, r, g, b)
    fun alpha(color: Int): Int = (color ushr 24) and 0xFF
    fun red(color: Int): Int = (color shr 16) and 0xFF
    fun green(color: Int): Int = (color shr 8) and 0xFF
    fun blue(color: Int): Int = color and 0xFF
    fun parseColor(colorString: String): Int = java.awt.Color.decode(colorString).rgb
    fun HSVToColor(hsv: FloatArray): Int = HSVToColor(255, hsv)
    fun HSVToColor(alpha: Int, hsv: FloatArray): Int {
        val c = java.awt.Color.getHSBColor(hsv[0] / 360f, hsv[1].coerceIn(0f, 1f), hsv[2].coerceIn(0f, 1f))
        return argb(alpha, c.red, c.green, c.blue)
    }
    fun colorToHSV(color: Int, hsv: FloatArray) {
        val c = java.awt.Color(red(color), green(color), blue(color))
        val hsb = java.awt.Color.RGBtoHSB(c.red, c.green, c.blue, null)
        hsv[0] = hsb[0] * 360f; hsv[1] = hsb[1]; hsv[2] = hsb[2]
    }
}

class Typeface private constructor(val name: String, val style: Int) {
    companion object {
        const val NORMAL = 0
        const val BOLD = 1
        const val ITALIC = 2
        const val BOLD_ITALIC = 3
        val DEFAULT: Typeface = Typeface("default", NORMAL)
        val DEFAULT_BOLD: Typeface = Typeface("default-bold", BOLD)
        val SANS_SERIF: Typeface = Typeface("sans-serif", NORMAL)
        val SERIF: Typeface = Typeface("serif", NORMAL)
        val MONOSPACE: Typeface = Typeface("monospace", NORMAL)

        fun create(familyName: String, style: Int): Typeface = Typeface(familyName, style)

        @JvmStatic
        fun createFromFile(path: java.io.File): Typeface = Typeface(path.name, NORMAL)

        @JvmStatic
        fun createFromFile(path: String): Typeface = Typeface(java.io.File(path).name, NORMAL)

        @JvmStatic
        fun createFromAsset(manager: Any?, path: String): Typeface = Typeface(path, NORMAL)
        fun create(family: Typeface?, style: Int): Typeface = Typeface(family?.name ?: "default", style)
        fun defaultFromStyle(style: Int): Typeface = Typeface("default", style)
    }
}