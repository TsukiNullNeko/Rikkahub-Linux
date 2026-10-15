package io.github.g00fy2.quickie

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import android.net.Uri
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import javax.imageio.ImageIO

/**
 * Desktop replacement for the Quickie QR scanner.
 *
 * Linux has no camera pipeline here, so the contract decodes a QR code from an image file
 * chosen by the user (zxing).
 */
sealed class QRResult {
    data class QRSuccess(val content: QRContent) : QRResult()
    data class QRError(val exception: Throwable) : QRResult()
    data object QRMissingPermission : QRResult()
    data object QRUserCanceled : QRResult()
}

data class QRContent(val rawValue: String?)

class ScanQRCode : ActivityResultContract<Any?, QRResult>() {
    override fun execute(input: Any?, context: Context?): QRResult {
        val uri = pickImage() ?: return QRResult.QRUserCanceled
        return decode(uri)
    }

    private fun pickImage(): Uri? {
        val chooser = javax.swing.JFileChooser().apply {
            dialogTitle = "Select a QR code image"
            fileFilter = javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "png", "jpg", "jpeg", "gif", "webp", "bmp"
            )
        }
        val result = chooser.showOpenDialog(
            java.awt.Window.getWindows().firstOrNull { it.isActive && it.isVisible }
        )
        if (result != javax.swing.JFileChooser.APPROVE_OPTION) return null
        return Uri.fromFile(chooser.selectedFile)
    }

    private fun decode(uri: Uri): QRResult {
        return try {
            val path = uri.path ?: return QRResult.QRError(IllegalArgumentException("invalid image"))
            val image = ImageIO.read(java.io.File(path))
                ?: return QRResult.QRError(IllegalArgumentException("unsupported image"))
            val source = BufferedImageLuminanceSource(image)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = MultiFormatReader().decode(bitmap)
            QRResult.QRSuccess(QRContent(result.text))
        } catch (e: Exception) {
            QRResult.QRError(e)
        }
    }
}