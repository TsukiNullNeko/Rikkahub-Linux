package android.webkit

object MimeTypeMap {
    private val map = mapOf(
        "txt" to "text/plain",
        "md" to "text/markdown",
        "html" to "text/html",
        "htm" to "text/html",
        "css" to "text/css",
        "csv" to "text/csv",
        "json" to "application/json",
        "xml" to "application/xml",
        "yaml" to "application/x-yaml",
        "yml" to "application/x-yaml",
        "pdf" to "application/pdf",
        "zip" to "application/zip",
        "png" to "image/png",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "gif" to "image/gif",
        "webp" to "image/webp",
        "svg" to "image/svg+xml",
        "bmp" to "image/bmp",
        "ico" to "image/x-icon",
        "mp3" to "audio/mpeg",
        "wav" to "audio/wav",
        "ogg" to "audio/ogg",
        "flac" to "audio/flac",
        "m4a" to "audio/mp4",
        "mp4" to "video/mp4",
        "webm" to "video/webm",
        "mkv" to "video/x-matroska",
        "doc" to "application/msword",
        "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "xls" to "application/vnd.ms-excel",
        "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "ppt" to "application/vnd.ms-powerpoint",
        "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "epub" to "application/epub+zip",
        "bin" to "application/octet-stream",
        "js" to "application/javascript",
        "woff" to "font/woff",
        "woff2" to "font/woff2",
        "ttf" to "font/ttf",
        "otf" to "font/otf"
    )

    fun getSingleton(): MimeTypeMap = this

    fun getMimeTypeFromExtension(extension: String?): String? =
        extension?.lowercase()?.let { map[it] }

    fun getExtensionFromMimeType(mimeType: String?): String? {
        val ext = mimeType?.let { mime -> map.entries.firstOrNull { it.value == mime }?.key }
        return ext
    }

    fun hasMimeType(mimeType: String?): Boolean =
        mimeType != null && map.containsValue(mimeType)

    fun hasExtension(extension: String?): Boolean =
        extension != null && map.containsKey(extension.lowercase())
}