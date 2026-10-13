package androidx.core.graphics

object ColorUtils {
    fun setAlphaColor(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha and 0xFF) shl 24)

    fun compositeColors(foreground: Int, background: Int): Int {
        val bgAlpha = android.graphics.Color.alpha(background) / 255.0
        val fgAlpha = android.graphics.Color.alpha(foreground) / 255.0
        val a = fgAlpha + bgAlpha * (1 - fgAlpha)
        if (a == 0.0) return 0
        val r = (android.graphics.Color.red(foreground) * fgAlpha +
            android.graphics.Color.red(background) * bgAlpha * (1 - fgAlpha)) / a
        val g = (android.graphics.Color.green(foreground) * fgAlpha +
            android.graphics.Color.green(background) * bgAlpha * (1 - fgAlpha)) / a
        val b = (android.graphics.Color.blue(foreground) * fgAlpha +
            android.graphics.Color.blue(background) * bgAlpha * (1 - fgAlpha)) / a
        return android.graphics.Color.argb((a * 255).toInt(), r.toInt(), g.toInt(), b.toInt())
    }

    fun calculateLuminance(color: Int): Double {
        fun channel(c: Int): Double {
            val v = c / 255.0
            return if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(android.graphics.Color.red(color)) +
            0.7152 * channel(android.graphics.Color.green(color)) +
            0.0722 * channel(android.graphics.Color.blue(color))
    }

    fun calculateContrast(foreground: Int, background: Int): Double {
        val l1 = calculateLuminance(foreground)
        val l2 = calculateLuminance(background)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    fun colorToHSL(color: Int, hsl: FloatArray) {
        val hsb = java.awt.Color.RGBtoHSB(
            android.graphics.Color.red(color),
            android.graphics.Color.green(color),
            android.graphics.Color.blue(color),
            null
        )
        hsl[0] = hsb[0] * 360f
        hsl[1] = hsb[1]
        hsl[2] = hsb[2]
    }

    fun HSLToColor(hsl: FloatArray): Int {
        val rgb = java.awt.Color.HSBtoRGB(hsl[0] / 360f, hsl[1].coerceIn(0f, 1f), hsl[2].coerceIn(0f, 1f))
        return rgb or (0xFF shl 24)
    }

    fun RGBToHSL(r: Int, g: Int, b: Int, hsl: FloatArray) {
        val hsb = java.awt.Color.RGBtoHSB(r, g, b, null)
        hsl[0] = hsb[0] * 360f
        hsl[1] = hsb[1]
        hsl[2] = hsb[2]
    }

    fun HSLToRGB(h: Float, s: Float, l: Float, rgb: FloatArray) {
        val color = java.awt.Color.HSBtoRGB(h / 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
        rgb[0] = android.graphics.Color.red(color).toFloat()
        rgb[1] = android.graphics.Color.green(color).toFloat()
        rgb[2] = android.graphics.Color.blue(color).toFloat()
    }

    fun blendARGB(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        return android.graphics.Color.argb(
            (android.graphics.Color.alpha(color1) * inverseRatio + android.graphics.Color.alpha(color2) * ratio).toInt(),
            (android.graphics.Color.red(color1) * inverseRatio + android.graphics.Color.red(color2) * ratio).toInt(),
            (android.graphics.Color.green(color1) * inverseRatio + android.graphics.Color.green(color2) * ratio).toInt(),
            (android.graphics.Color.blue(color1) * inverseRatio + android.graphics.Color.blue(color2) * ratio).toInt()
        )
    }
}