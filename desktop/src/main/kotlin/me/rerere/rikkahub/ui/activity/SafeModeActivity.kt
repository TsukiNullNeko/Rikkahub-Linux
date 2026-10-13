package me.rerere.rikkahub.ui.activity

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Android safe-mode entry point (launches the app with a clean configuration).
 * The desktop build starts normally, so this is a no-op screen.
 */
object SafeModeActivity {
    const val EXTRA_SAFE_MODE = "safe_mode"

    @Composable
    fun Content() {
        Text(
            text = "Safe mode is not required on Linux.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
