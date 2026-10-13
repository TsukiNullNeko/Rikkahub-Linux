package me.rerere.rikkahub.ui.components.ui

import androidx.compose.runtime.Composable

/**
 * Keeps the screen awake while [enabled].
 *
 * Desktop sessions have no such constraint (the display sleep policy is owned by the desktop
 * environment), so this is intentionally a no-op.
 */
@Composable
fun KeepScreenOn(enabled: Boolean = true) {
    // no-op on desktop
}
