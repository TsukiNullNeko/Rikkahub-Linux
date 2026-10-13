package android.media

import java.io.File
import java.io.InputStream

/**
 * Minimal EXIF reader for the orientation tag, backed by metadata-extractor.
 */
class ExifInterface {
    companion object {
        const val TAG_ORIENTATION = "Orientation"
        const val ORIENTATION_UNDEFINED = 0
        const val ORIENTATION_NORMAL = 1
        const val ORIENTATION_FLIP_HORIZONTAL = 2
        const val ORIENTATION_ROTATE_180 = 3
        const val ORIENTATION_FLIP_VERTICAL = 4
        const val ORIENTATION_TRANSPOSE = 5
        const val ORIENTATION_ROTATE_90 = 6
        const val ORIENTATION_TRANSVERSE = 7
        const val ORIENTATION_ROTATE_270 = 8
        const val WHITEBALANCE_AUTO = 0
        const val WHITEBALANCE_MANUAL = 1
    }

    private val values = mutableMapOf<String, String>()

    constructor(filePath: String) {
        load(File(filePath))
    }

    constructor(file: File) {
        load(file)
    }

    constructor(inputStream: InputStream) {
        loadStream(inputStream)
    }

    private fun load(file: File) {
        if (!file.exists()) return
        try {
            loadStream(file.inputStream())
        } catch (e: Exception) {
            android.util.Log.w("ExifInterface", "failed to read exif", e)
        }
    }

    private fun loadStream(stream: InputStream) {
        try {
            val metadata = com.drew.imaging.ImageMetadataReader.readMetadata(stream)
            val dir = metadata.getFirstDirectoryOfType(com.drew.metadata.exif.ExifIFD0Directory::class.java)
            val orientation = dir?.getInt(com.drew.metadata.exif.ExifIFD0Directory.TAG_ORIENTATION)
            if (orientation != null) values[TAG_ORIENTATION] = orientation.toString()
        } catch (e: Exception) {
            android.util.Log.w("ExifInterface", "no exif metadata", e)
        }
    }

    fun getAttribute(tag: String): String? = values[tag]

    fun getAttributeInt(tag: String, defaultValue: Int): Int = values[tag]?.toIntOrNull() ?: defaultValue

    fun getAttributeDouble(tag: String, defaultValue: Double): Double = values[tag]?.toDoubleOrNull() ?: defaultValue

    fun setAttribute(tag: String, value: String?) {
        if (value == null) values.remove(tag) else values[tag] = value
    }

    fun saveAttributes() {}

    fun getLatLong(output: FloatArray): Boolean = false

    fun rotate(degrees: Float) {}
}
