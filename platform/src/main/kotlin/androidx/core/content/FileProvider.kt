package androidx.core.content

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Desktop stand-in for androidx FileProvider: file URIs are already shareable on Linux,
 * so this simply returns a file:// URI.
 */
open class FileProvider : android.content.ContentProvider() {
    companion object {
        @JvmStatic
        fun getUriForFile(context: Context, authority: String, file: File): Uri = Uri.fromFile(file)

        @JvmStatic
        fun getUriForFile(context: Context, authority: String, file: File, displayName: String): Uri =
            Uri.fromFile(file)
    }
}

// Minimal ContentProvider base so the shim compiles without the Android framework
abstract class ContentProviderShim