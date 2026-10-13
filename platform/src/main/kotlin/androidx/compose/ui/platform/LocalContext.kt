package androidx.compose.ui.platform

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf

/** Desktop equivalent of the Android LocalContext; provides the app Context. */
val LocalContext: androidx.compose.runtime.ProvidableCompositionLocal<Context> =
    staticCompositionLocalOf { android.content.AppContext.appContext }

/** Placeholder LocalView for source compatibility. */
class LocalView {
    companion object {
        val current: Any? get() = null
    }
}
