package android.content

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

class ClipData private constructor(val description: CharSequence?) {
    val items = mutableListOf<Item>()
    val itemCount: Int get() = items.size

    class Item(val text: CharSequence?, val uri: android.net.Uri? = null) {
        fun coerceToText(context: Context?): CharSequence = text ?: uri?.toString() ?: ""
        companion object {
            fun newPlainText(label: CharSequence?, text: CharSequence?) = Item(text)
            fun newUri(context: Context?, label: CharSequence?, uri: android.net.Uri?) = Item(uri?.toString(), uri)
        }
    }

    companion object {
        fun newPlainText(label: CharSequence?, text: CharSequence?): ClipData {
            val cd = ClipData(label)
            cd.items.add(Item(text))
            return cd
        }
        fun newUri(context: Context?, label: CharSequence?, uri: android.net.Uri?): ClipData {
            val cd = ClipData(label)
            cd.items.add(Item(uri?.toString(), uri))
            return cd
        }
        fun newIntent(label: CharSequence?, intent: Intent?): ClipData {
            val cd = ClipData(label)
            cd.items.add(Item(intent?.toString()))
            return cd
        }
    }
}

class ClipboardManager {
    fun setPrimaryClip(clip: ClipData) {
        try {
            val text = clip.items.firstOrNull()?.text?.toString() ?: ""
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
        } catch (e: Exception) {
            android.util.Log.w("Clipboard", "setPrimaryClip failed", e)
        }
    }

    val primaryClip: ClipData?
        get() {
            return try {
                val text = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as? String
                if (text != null) ClipData.newPlainText(null, text) else null
            } catch (e: Exception) {
                null
            }
        }

    val primaryClipDescription: ClipDescription? get() = ClipDescription()

    fun hasPrimaryClip(): Boolean = primaryClip != null

    fun addPrimaryClipChangedListener(listener: ClipboardManager.OnPrimaryClipChangedListener) {}
    fun removePrimaryClipChangedListener(listener: ClipboardManager.OnPrimaryClipChangedListener) {}

    fun interface OnPrimaryClipChangedListener {
        fun onPrimaryClipChanged()
    }
}

class ClipDescription {
    fun getMimeType(index: Int): String = "text/plain"
}
