package me.rerere.rikkahub.ui.components.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.ui.context.LocalSettings

/**
 * Loading indicator. The Android build animates an app-icon vector drawable; desktop uses
 * Material's loading indicators.
 */
@Composable
fun RabbitLoadingIndicator(modifier: Modifier = Modifier) {
    val useAppIconStyle = LocalSettings.current.displaySetting.useAppIconStyleLoadingIndicator
    if (useAppIconStyle) {
        ContainedLoadingIndicator(modifier = modifier)
    } else {
        CircularProgressIndicator(
            modifier = modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
