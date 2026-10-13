package androidx.compose.foundation.content

import android.net.Uri
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

/** A single clipboard/drag item: either a file (image) or text. */
class ClipItem(val uri: Uri?, val text: CharSequence?)

/**
 * Desktop implementation of the content-transfer helper: reads the system clipboard
 * (file list first, then plain text) and stops once [block] accepts an item.
 */
fun TransferableContent.consume(block: (ClipItem) -> Boolean) {
    val clipboard = try {
        Toolkit.getDefaultToolkit().systemClipboard
    } catch (e: Exception) {
        return
    }

    try {
        if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
            @Suppress("UNCHECKED_CAST")
            val files = clipboard.getData(DataFlavor.javaFileListFlavor) as? List<Any?> ?: emptyList()
            for (file in files) {
                if (file is java.io.File) {
                    if (block(ClipItem(Uri.fromFile(file), null))) return
                }
            }
        }
    } catch (_: Exception) {
    }

    try {
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            val text = clipboard.getData(DataFlavor.stringFlavor) as? String
            if (text != null) {
                block(ClipItem(null, text))
            }
        }
    } catch (_: Exception) {
    }
}
