package me.rerere.ui.sketch

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Cancel01
import me.rerere.hugeicons.stroke.Delete02
import me.rerere.hugeicons.stroke.Redo
import me.rerere.hugeicons.stroke.Tick02
import me.rerere.hugeicons.stroke.Undo
import me.rerere.ui.R
import java.io.File
import javax.imageio.ImageIO

@Composable
fun SketchDialog(
    onDismiss: () -> Unit,
    onConfirm: (SketchResult) -> Unit,
    image: Uri? = null,
    aspectRatio: Float? = null,
) {
    val state = remember { SketchState(aspectRatio) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(image) {
        if (image != null) {
            val path = image.path
            if (path != null) {
                runCatching { ImageIO.read(File(path)) }.getOrNull()?.let { state.background = it }
            }
        }
    }

    fun close() {
        if (state.isBlank) onDismiss() else confirmDiscard = true
    }

    Dialog(
        onDismissRequest = { close() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { state.undo() }, enabled = state.canUndo) {
                        Icon(HugeIcons.Undo, contentDescription = stringResource(R.string.sketch_dialog_undo))
                    }
                    IconButton(onClick = { state.redo() }, enabled = state.canRedo) {
                        Icon(HugeIcons.Redo, contentDescription = stringResource(R.string.sketch_dialog_redo))
                    }
                    IconButton(onClick = { state.clear() }, enabled = state.canUndo) {
                        Icon(HugeIcons.Delete02, contentDescription = stringResource(R.string.sketch_dialog_clear))
                    }
                    TextButton(onClick = { state.erasing = !state.erasing }) {
                        Text(stringResource(R.string.sketch_dialog_eraser))
                    }

                    SketchDefaults.colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (state.color == c && !state.erasing) 3.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable {
                                    state.color = c
                                    state.erasing = false
                                }
                        )
                    }

                    SketchDefaults.widths.forEach { w ->
                        IconButton(onClick = { state.width = w; state.erasing = false }) {
                            Box(
                                modifier = Modifier
                                    .size((w + 6).dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (state.width == w) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { close() }) {
                        Icon(HugeIcons.Cancel01, contentDescription = null)
                    }
                    FilledIconButton(onClick = {
                        val bytes = state.encode(
                            canvasSize.width.coerceAtLeast(1),
                            canvasSize.height.coerceAtLeast(1)
                        )
                        onConfirm(SketchResult(bytes))
                    }) {
                        Icon(HugeIcons.Tick02, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.White)
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(state.erasing, state.color, state.width) {
                            detectDragGestures(
                                onDragStart = { state.beginStroke(it) },
                                onDrag = { change, _ ->
                                    state.extendStroke(change.position)
                                    change.consume()
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        state.background?.let { bg ->
                            drawImage(
                                image = bg.toComposeImageBitmap(),
                                dstSize = IntSize(size.width.toInt(), size.height.toInt())
                            )
                        }
                        state.strokes.forEach { stroke ->
                            val strokeColor = if (stroke.color == Color.Transparent) Color.White else stroke.color
                            if (stroke.points.size == 1) {
                                drawCircle(
                                    color = strokeColor,
                                    radius = stroke.width / 2f,
                                    center = stroke.points.first()
                                )
                            } else {
                                for (i in 0 until stroke.points.size - 1) {
                                    drawLine(
                                        color = strokeColor,
                                        start = stroke.points[i],
                                        end = stroke.points[i + 1],
                                        strokeWidth = stroke.width,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(R.string.sketch_dialog_discard_title)) },
            text = { Text(stringResource(R.string.sketch_dialog_discard_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    onDismiss()
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text("Cancel") }
            }
        )
    }
}
