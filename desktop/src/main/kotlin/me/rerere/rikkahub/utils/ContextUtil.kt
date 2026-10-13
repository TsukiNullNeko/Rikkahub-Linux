package me.rerere.rikkahub.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.File

private const val TAG = "ContextUtil"

fun Context.readClipboardText(): String {
    return try {
        Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as? String ?: ""
    } catch (e: Exception) {
        Log.w(TAG, "failed to read clipboard", e)
        ""
    }
}

fun Context.writeClipboardText(text: String) {
    try {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    } catch (e: Exception) {
        Log.w(TAG, "failed to write clipboard", e)
    }
}

fun Context.joinQQGroup(key: String?): Boolean {
    if (key.isNullOrBlank()) return false
    openUrl("https://qm.qq.com/cgi-bin/qm/qr?k=$key")
    return true
}

/** Linux has no per-app usage statistics; screen-time features are inert. */
fun Context.hasUsageStatsPermission(): Boolean = false

fun Context.openUsageAccessSettings() {
    // Not applicable on Linux.
}

fun Context.openUrl(url: String) {
    try {
        ProcessBuilder("xdg-open", url).start()
    } catch (e: Exception) {
        Log.e(TAG, "failed to open url: $url", e)
    }
}

fun Context.getActivity(): Activity? = this as? Activity

fun Context.getComponentActivity(): Activity? = this as? Activity

private fun picturesDir(): File =
    File(System.getProperty("user.home")!!, "Pictures").also { it.mkdirs() }

/** Saves a bitmap into the user's Pictures directory. */
fun Context.exportImage(
    activity: Activity,
    bitmap: Bitmap,
    fileName: String = "RikkaHub_" + System.currentTimeMillis() + ".png"
) {
    try {
        val target = File(picturesDir(), fileName)
        target.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        Log.i(TAG, "image saved: " + target.absolutePath)
    } catch (e: Exception) {
        Log.e(TAG, "failed to save image", e)
    }
}

/** Copies an image file into the user's Pictures directory. */
fun Context.exportImageFile(
    activity: Activity,
    file: File,
    fileName: String = "RikkaHub_" + System.currentTimeMillis() + ".png"
) {
    try {
        val target = File(picturesDir(), fileName)
        file.copyTo(target, overwrite = true)
        Log.i(TAG, "image saved: " + target.absolutePath)
    } catch (e: Exception) {
        Log.e(TAG, "failed to save image file", e)
    }
}
