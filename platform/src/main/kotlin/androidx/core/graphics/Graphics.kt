package androidx.core.graphics

import android.graphics.Bitmap

fun createBitmap(
    width: Int,
    height: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap = Bitmap.createBitmap(width, height, config)

/** Pixel assignment operator, mirroring androidx.core.graphics.set. */
operator fun android.graphics.Bitmap.set(x: Int, y: Int, color: Int) {
    setPixel(x, y, color)
}

/** Pixel read operator. */
operator fun android.graphics.Bitmap.get(x: Int, y: Int): Int = getPixel(x, y)
