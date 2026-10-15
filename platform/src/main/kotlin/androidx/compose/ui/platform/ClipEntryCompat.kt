package androidx.compose.ui.platform

import android.content.ClipData

/**
 * Desktop helper exposing clipboard text through the Android-style ClipData wrapper.
 */
val ClipEntry.clipData: ClipData?
    get() {
        val native = nativeClipEntry
        return when (native) {
            is java.awt.datatransfer.StringSelection -> ClipData.newPlainText(null, null)
            is String -> ClipData.newPlainText(null, native)
            null -> null
            else -> ClipData.newPlainText(null, native.toString())
        }
    }
