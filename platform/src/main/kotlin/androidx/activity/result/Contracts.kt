package androidx.activity.result.contract

import android.content.Context
import android.net.Uri
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementations of the Android activity-result contracts.
 * File pickers use Swing's JFileChooser, which works on X11 and Wayland desktops.
 */
abstract class ActivityResultContract<I : Any?, O : Any?> {
    /** Executes the contract and returns the result. Runs on a background thread. */
    abstract fun execute(input: I, context: Context?): O
}

object ActivityResultContracts {
    class GetContent : ActivityResultContract<String, Uri?>() {
        override fun execute(input: String, context: Context?): Uri? =
            FileDialogHelper.pickFile(input, false)
    }

    class GetMultipleContents : ActivityResultContract<String, List<Uri>>() {
        override fun execute(input: String, context: Context?): List<Uri> =
            FileDialogHelper.pickFiles(input) ?: emptyList()
    }

    class OpenDocument : ActivityResultContract<Array<String>, Uri?>() {
        override fun execute(input: Array<String>, context: Context?): Uri? =
            FileDialogHelper.pickFile(input.firstOrNull(), false)
    }

    class OpenMultipleDocuments : ActivityResultContract<Array<String>, List<Uri>>() {
        override fun execute(input: Array<String>, context: Context?): List<Uri> =
            FileDialogHelper.pickFiles(input.firstOrNull()) ?: emptyList()
    }

    class OpenDocumentTree : ActivityResultContract<Uri?, Uri?>() {
        override fun execute(input: Uri?, context: Context?): Uri? =
            FileDialogHelper.pickDirectory()
    }

    class CreateDocument(private val mimeType: String = "application/octet-stream") : ActivityResultContract<String, Uri?>() {
        override fun execute(input: String, context: Context?): Uri? =
            FileDialogHelper.saveFile(input)
    }

    class PickVisualMedia : ActivityResultContract<PickVisualMediaRequest, Uri?>() {
        override fun execute(input: PickVisualMediaRequest, context: Context?): Uri? =
            FileDialogHelper.pickFile("image/*", false)
    }

    class PickMultipleVisualMedia(maxItems: Int = 99) : ActivityResultContract<PickVisualMediaRequest, List<Uri>>() {
        override fun execute(input: PickVisualMediaRequest, context: Context?): List<Uri> =
            FileDialogHelper.pickFiles("image/*") ?: emptyList()
    }

    class RequestPermission : ActivityResultContract<String, Boolean>() {
        override fun execute(input: String, context: Context?): Boolean = true
    }

    class RequestMultiplePermissions : ActivityResultContract<Array<String>, Map<String, Boolean>>() {
        override fun execute(input: Array<String>, context: Context?): Map<String, Boolean> =
            input.associateWith { true }
    }

    class TakePicture : ActivityResultContract<Uri, Boolean>() {
        override fun execute(input: Uri, context: Context?): Boolean = false
    }

    class StartActivityForResult : ActivityResultContract<android.content.Intent, ActivityResult?>() {
        override fun execute(input: android.content.Intent, context: Context?): ActivityResult? {
            android.content.AppContext.startActivity(input)
            return null
        }
    }
}

class ActivityResult(val resultCode: Int, val data: android.content.Intent?)

class PickVisualMediaRequest(val mediaType: Any?) {
    companion object {
        fun Builder(mediaType: Any? = null) = PickVisualMediaRequestBuilder(mediaType)
    }
}

class PickVisualMediaRequestBuilder(private val mediaType: Any?) {
    fun setMediaType(type: Any?): PickVisualMediaRequestBuilder = this
    fun build(): PickVisualMediaRequest = PickVisualMediaRequest(mediaType)
}

object PickVisualMedia {
    sealed class VisualMediaType
    val ImageOnlyType: VisualMediaType = ImageOnly
    object ImageOnly : VisualMediaType()
    object VideoOnly : VisualMediaType()
    object ImageAndVideo : VisualMediaType()
    class SingleMimeType(val mimeType: String) : VisualMediaType()

    const val ACTION_PICK_IMAGES = "pick_images"
}

internal object FileDialogHelper {
    private fun parentWindow(): java.awt.Window? =
        java.awt.Window.getWindows().firstOrNull { it.isActive && it.isVisible }
            ?: java.awt.Window.getWindows().firstOrNull { it.isVisible }

    private fun mimeToExtensions(mime: String?): Array<String>? {
        if (mime == null) return null
        return when {
            mime.startsWith("image/") -> arrayOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg")
            mime.startsWith("video/") -> arrayOf("mp4", "webm", "mkv", "mov")
            mime.startsWith("audio/") -> arrayOf("mp3", "wav", "ogg", "flac", "m4a")
            mime == "application/pdf" -> arrayOf("pdf")
            mime.startsWith("text/") -> arrayOf("txt", "md", "csv", "json", "html")
            else -> null
        }
    }

    fun pickFile(mime: String?, multi: Boolean): Uri? {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Select file"
        chooser.isMultiSelectionEnabled = false
        mimeToExtensions(mime)?.let {
            chooser.fileFilter = FileNameExtensionFilter("Supported files", *it)
        }
        val result = chooser.showOpenDialog(parentWindow())
        if (result != JFileChooser.APPROVE_OPTION) return null
        return chooser.selectedFile?.let { Uri.fromFile(it) }
    }

    fun pickFiles(mime: String?): List<Uri>? {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Select files"
        chooser.isMultiSelectionEnabled = true
        mimeToExtensions(mime)?.let {
            chooser.fileFilter = FileNameExtensionFilter("Supported files", *it)
        }
        val result = chooser.showOpenDialog(parentWindow())
        if (result != JFileChooser.APPROVE_OPTION) return null
        return chooser.selectedFiles?.map { Uri.fromFile(it) }
    }

    fun pickDirectory(): Uri? {
        val chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogTitle = "Select folder"
        val result = chooser.showOpenDialog(parentWindow())
        if (result != JFileChooser.APPROVE_OPTION) return null
        return Uri.fromFile(chooser.selectedFile)
    }

    fun saveFile(suggestedName: String?): Uri? {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Save file"
        if (!suggestedName.isNullOrBlank()) chooser.selectedFile = java.io.File(suggestedName)
        val result = chooser.showSaveDialog(parentWindow())
        if (result != JFileChooser.APPROVE_OPTION) return null
        return Uri.fromFile(chooser.selectedFile)
    }
}