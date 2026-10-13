package android.content

class ContentResolver {
    companion object {
        const val SCHEME_FILE = "file"
        const val SCHEME_CONTENT = "content"
        const val SCHEME_ANDROID_RESOURCE = "android.resource"
    }

    fun openInputStream(uri: android.net.Uri): java.io.InputStream? {
        val path = uri.path ?: return null
        val file = java.io.File(path)
        if (file.exists()) return file.inputStream()
        return null
    }

    fun openOutputStream(uri: android.net.Uri, mode: String = "w"): java.io.OutputStream? {
        val path = uri.path ?: return null
        val file = java.io.File(path)
        file.parentFile?.mkdirs()
        return file.outputStream()
    }

    fun query(
        uri: android.net.Uri,
        projection: Array<String>?,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null
    ): Cursor? {
        val path = uri.path ?: return null
        val file = java.io.File(path)
        val cursor = MatrixCursor(arrayOf("_display_name", "_size", "mime_type", "_data"))
        if (file.exists()) {
            cursor.addRow(arrayOf(file.name, file.length(), guessMimeType(file.name), file.absolutePath))
        }
        return cursor
    }

    fun insert(uri: android.net.Uri, values: ContentValues?): android.net.Uri? = null

    fun delete(uri: android.net.Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    fun update(uri: android.net.Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0

    fun call(uri: android.net.Uri, method: String, arg: String?, extras: android.os.Bundle?): android.os.Bundle? = null

    fun getType(uri: android.net.Uri): String? {
        val path = uri.path ?: return null
        return guessMimeType(path)
    }

    private fun guessMimeType(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }
}
