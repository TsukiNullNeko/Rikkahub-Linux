package me.rerere.rikkahub

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RikkaHub",
        state = rememberWindowState(width = 1280.dp, height = 800.dp)
    ) {
        MaterialTheme {
            Text("RikkaHub Linux - toolchain check")
        }
    }
}
