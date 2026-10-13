package androidx.activity.compose

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Desktop stand-in for LocalActivity. Provides null on desktop unless the app
 * explicitly provides an Activity-like object.
 */
val LocalActivity: androidx.compose.runtime.ProvidableCompositionLocal<Any?> =
    staticCompositionLocalOf { null }
