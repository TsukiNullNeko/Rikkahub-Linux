package me.rerere.rikkahub.ui.components.ai

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Image cropping launcher.
 *
 * The Linux build has no in-app cropper; images are passed through unchanged.
 */
@Composable
internal fun useCropLauncher(
    onCroppedImageReady: (Uri) -> Unit,
    onCleanup: (() -> Unit)? = null,
    aspectRatio: Pair<Float, Float>? = null,
    freeStyleCropEnabled: Boolean = true
): Pair<Any?, (Uri) -> Unit> {
    val launcher: (Uri) -> Unit = remember(onCroppedImageReady) {
        { uri ->
            onCroppedImageReady(uri)
            onCleanup?.invoke()
        }
    }
    return null to launcher
}
