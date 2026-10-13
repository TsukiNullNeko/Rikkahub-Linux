package me.rerere.rikkahub.ui.components.ui

import androidx.compose.runtime.Composable

/**
 * Android floating overlay window.
 *
 * Linux window managers do not allow arbitrary overlay windows, so the desktop build renders
 * nothing here; the same content stays reachable from the in-app UI.
 */
@Composable
fun FloatingWindow(
    tag: String,
    visibility: Boolean = true,
    content: @Composable () -> Unit,
) {
    // Intentionally empty on desktop.
}
