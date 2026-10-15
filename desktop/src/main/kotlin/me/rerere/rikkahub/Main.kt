package me.rerere.rikkahub

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import me.rerere.rikkahub.ui.theme.RikkahubTheme
import java.awt.Toolkit

/**
 * RikkaHub desktop entry point.
 */
fun main(args: Array<String>) {
    // AWT needs a headless-safe toolkit for tray icons and dialogs.
    System.setProperty("apple.awt.application.name", "RikkaHub")
    // GL 在部分 Wayland/无 GPU 环境不可用，失败时自动回退软件渲染
    if (System.getenv("RIKKAHUB_SOFTWARE_RENDER") == "1") {
        System.setProperty("skiko.renderApi", "SOFTWARE")
    }

    RikkaHubApp.init()

    application {
        var windowVisible by remember { mutableStateOf(true) }
        val windowState = rememberWindowState(size = DpSize(1280.dp, 860.dp))
        val trayState = rememberTrayState()

        val appIcon = painterResource(R.mipmap.ic_launcher)

        if (isTraySupported) {
        Tray(
            icon = appIcon,
            state = trayState,
            tooltip = "RikkaHub",
            onAction = {
                windowVisible = true
            },
            menu = {
                Item("Show RikkaHub", onClick = { windowVisible = true })
                Item("Quit", onClick = { RikkaHubApp.shutdown(); exitApplication() })
            }
        )
        }

        if (windowVisible) {
            Window(
                onCloseRequest = {
                    // Keep running in the tray; use the tray menu to quit.
                    windowVisible = false
                },
                title = "RikkaHub",
                state = windowState,
                icon = appIcon,
                onKeyEvent = { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                        androidx.activity.compose.BackDispatcher.handleBack()
                    } else {
                        false
                    }
                }
            ) {
                RikkahubTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AppRoutes()
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            // Give the composition a frame to settle before showing the window.
            windowVisible = true
        }
    }
}

private fun loadAppIcon(): java.awt.Image {
    return runCatching {
        val stream = Thread.currentThread().contextClassLoader
            ?.getResourceAsStream("res/drawable/ic_launcher.png")
            ?: object {}.javaClass.getResourceAsStream("/res/drawable/ic_launcher.png")
        if (stream != null) {
            javax.imageio.ImageIO.read(stream)
        } else {
            Toolkit.getDefaultToolkit().createImage(ByteArray(0))
        }
    }.getOrElse {
        Toolkit.getDefaultToolkit().createImage(ByteArray(0))
    }
}