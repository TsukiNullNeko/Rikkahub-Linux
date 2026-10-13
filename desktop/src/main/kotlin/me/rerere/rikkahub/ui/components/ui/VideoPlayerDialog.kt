package me.rerere.rikkahub.ui.components.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Plays a local video file.
 *
 * Desktop builds hand the file to the system video player (xdg-open) instead of embedding
 * an ExoPlayer surface.
 */
@Composable
fun VideoPlayerDialog(
    file: File,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Video") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Playback uses the system video player on Linux.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                context.openUri(android.net.Uri.fromFile(file))
                onDismissRequest()
            }) {
                Text("Open")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Close") }
        },
        modifier = Modifier.padding(8.dp)
    )
}
