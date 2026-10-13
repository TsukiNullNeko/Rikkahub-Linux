package me.rerere.rikkahub.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.dokar.sonner.ToastType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Download01
import me.rerere.rikkahub.ui.context.LocalToaster
import java.io.File
import java.net.URI

/**
 * Full-screen image preview with swipe/arrow paging and a save-to-Pictures action.
 */
@Composable
fun ImagePreviewDialog(
    images: List<String>,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { images.size })

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(1f)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = {
                    scope.launch {
                        val current = images.getOrNull(pagerState.currentPage) ?: return@launch
                        val saved = withContext(Dispatchers.IO) { saveToPictures(current) }
                        toaster.show(
                            if (saved != null) "Saved to " + saved.parent else "Failed to save image",
                            type = if (saved != null) ToastType.Success else ToastType.Error
                        )
                    }
                }) {
                    Icon(HugeIcons.Download01, contentDescription = "Save image", tint = Color.White)
                }
            }
        }
    }
}

private fun saveToPictures(source: String): File? {
    return runCatching {
        val pictures = File(System.getProperty("user.home")!!, "Pictures").also { it.mkdirs() }
        val target = File(pictures, "RikkaHub_" + System.currentTimeMillis() + ".png")
        when {
            source.startsWith("file://") -> {
                val file = File(URI(source))
                file.copyTo(target, overwrite = true)
            }
            source.startsWith("content://") -> return null
            else -> {
                val url = java.net.URL(source)
                url.openStream().use { input -> target.outputStream().use { input.copyTo(it) } }
            }
        }
        target
    }.getOrNull()
}
