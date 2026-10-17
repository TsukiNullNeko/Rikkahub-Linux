package me.rerere.rikkahub

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.rerere.rikkahub.ui.theme.RikkahubTheme
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import javax.imageio.ImageIO

/**
 * RikkaHub desktop entry point.
 */
fun main(args: Array<String>) {
    System.setProperty("apple.awt.application.name", "RikkaHub")
    // On Wayland / GPU-less environments Skiko's GL pipeline may be unavailable;
    // set RIKKAHUB_SOFTWARE_RENDER=1 to force software rendering.
    if (System.getenv("RIKKAHUB_SOFTWARE_RENDER") == "1") {
        System.setProperty("skiko.renderApi", "SOFTWARE")
    }

    RikkaHubApp.init()

    application {
        var windowVisible by remember { mutableStateOf(true) }
        var trayInstalled by remember { mutableStateOf(false) }
        val windowState = rememberWindowState(size = DpSize(1280.dp, 860.dp))
        val appIcon = painterResource(R.mipmap.ic_launcher)

        // The tray is created with plain AWT: Compose's Tray(painter) converts the painter to an
        // AWT image on the AWT thread, which requires the Compose graphics registry to be ready.
        DisposableEffect(Unit) {
            val trayIcon = installTray(
                onShow = { windowVisible = true },
                onQuit = {
                    RikkaHubApp.shutdown()
                    exitApplication()
                }
            )
            trayInstalled = trayIcon != null
            onDispose {
                trayIcon?.let { icon ->
                    runCatching { SystemTray.getSystemTray().remove(icon) }
                }
            }
        }

        if (windowVisible) {
            Window(
                onCloseRequest = {
                    if (trayInstalled) {
                        // Keep the app running in the tray.
                        windowVisible = false
                    } else {
                        RikkaHubApp.shutdown()
                        exitApplication()
                    }
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
    }
}

/**
 * Installs a system-tray icon through AWT (no Compose graphics involved).
 * Returns null when the desktop environment has no tray support.
 */
private fun installTray(onShow: () -> Unit, onQuit: () -> Unit): TrayIcon? {
    if (!SystemTray.isSupported()) return null
    return runCatching {
        val image = loadAppImage() ?: return null
        val icon = TrayIcon(image, "RikkaHub").apply {
            isImageAutoSize = true
            addActionListener { onShow() }
            popupMenu = PopupMenu().apply {
                add(MenuItem("显示 RikkaHub").apply { addActionListener { onShow() } })
                addSeparator()
                add(MenuItem("退出").apply { addActionListener { onQuit() } })
            }
        }
        SystemTray.getSystemTray().add(icon)
        icon
    }.onFailure {
        android.util.Log.w("RikkaHub", "tray icon unavailable: " + it.message)
    }.getOrNull()
}

private fun loadAppImage(): Image? = runCatching {
    val stream = Thread.currentThread().contextClassLoader
        ?.getResourceAsStream("res/drawable/ic_launcher.png")
        ?: object {}.javaClass.getResourceAsStream("/res/drawable/ic_launcher.png")
        ?: return null
    stream.use { ImageIO.read(it) }
}.getOrNull()
