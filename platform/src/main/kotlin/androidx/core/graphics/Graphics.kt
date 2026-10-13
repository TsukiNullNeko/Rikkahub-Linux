package androidx.core.graphics

import android.graphics.Bitmap

fun createBitmap(
    width: Int,
    height: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap = Bitmap.createBitmap(width, height, config)
