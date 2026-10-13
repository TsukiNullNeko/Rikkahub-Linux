package androidx.activity.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Desktop back dispatcher. The app window routes ESC / window-close intents here;
 * the last registered enabled handler wins (stack order).
 */
object BackDispatcher {
    private val handlers = ArrayDeque<Pair<Any, () -> Unit>>()

    fun handleBack(token: Any? = null): Boolean {
        val entry = handlers.lastOrNull { it.first != token }
        return if (entry != null) {
            entry.second()
            true
        } else {
            false
        }
    }

    fun register(token: Any, callback: () -> Unit) {
        handlers.addLast(token to callback)
    }

    fun unregister(token: Any) {
        handlers.removeAll { it.first == token }
    }
}

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val currentOnBack by rememberUpdatedState(onBack)
    val token = remember { Any() }
    DisposableEffect(enabled) {
        if (enabled) {
            BackDispatcher.register(token) { currentOnBack() }
        }
        onDispose {
            BackDispatcher.unregister(token)
        }
    }
}

/** Alias used by NavDisplay; window-level ESC handling dispatches into BackDispatcher. */
@Composable
fun EscBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}
