package androidx.exifinterface.media

import java.io.File
import java.io.InputStream

/**
 * Desktop shim for androidx ExifInterface, backed by the platform metadata reader.
 */
class ExifInterface {
    private val delegate: android.media.ExifInterface

    constructor(filePath: String) {
        delegate = android.media.ExifInterface(filePath)
    }

    constructor(file: File) {
        delegate = android.media.ExifInterface(file)
    }

    constructor(inputStream: InputStream) {
        delegate = android.media.ExifInterface(inputStream)
    }

    fun getAttribute(tag: String): String? = delegate.getAttribute(tag)

    fun getAttributeInt(tag: String, defaultValue: Int): Int = delegate.getAttributeInt(tag, defaultValue)

    fun getAttributeDouble(tag: String, defaultValue: Double): Double = delegate.getAttributeDouble(tag, defaultValue)

    fun setAttribute(tag: String, value: String?) = delegate.setAttribute(tag, value)

    fun saveAttributes() = delegate.saveAttributes()

    fun getLatLong(output: FloatArray): Boolean = delegate.getLatLong(output)

    fun rotate(degrees: Float) = delegate.rotate(degrees)

    companion object {
        const val TAG_ORIENTATION = android.media.ExifInterface.TAG_ORIENTATION
        const val ORIENTATION_UNDEFINED = android.media.ExifInterface.ORIENTATION_UNDEFINED
        const val ORIENTATION_NORMAL = android.media.ExifInterface.ORIENTATION_NORMAL
        const val ORIENTATION_FLIP_HORIZONTAL = android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL
        const val ORIENTATION_ROTATE_180 = android.media.ExifInterface.ORIENTATION_ROTATE_180
        const val ORIENTATION_FLIP_VERTICAL = android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL
        const val ORIENTATION_TRANSPOSE = android.media.ExifInterface.ORIENTATION_TRANSPOSE
        const val ORIENTATION_ROTATE_90 = android.media.ExifInterface.ORIENTATION_ROTATE_90
        const val ORIENTATION_TRANSVERSE = android.media.ExifInterface.ORIENTATION_TRANSVERSE
        const val ORIENTATION_ROTATE_270 = android.media.ExifInterface.ORIENTATION_ROTATE_270
        const val WHITEBALANCE_AUTO = 0
        const val WHITEBALANCE_MANUAL = 1
        const val TAG_DATETIME = "DateTime"
        const val TAG_MAKE = "Make"
        const val TAG_MODEL = "Model"
        const val TAG_IMAGE_WIDTH = "ImageWidth"
        const val TAG_IMAGE_LENGTH = "ImageLength"
    }
}
