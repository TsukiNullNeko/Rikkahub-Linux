package androidx.compose.ui.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** Desktop equivalent of the Android LocalResources composition local. */
val LocalResources: androidx.compose.runtime.ProvidableCompositionLocal<android.content.res.Resources> =
    staticCompositionLocalOf { android.content.AppContext.resources }

/** Desktop equivalent of LocalConfiguration. */
val LocalConfiguration: androidx.compose.runtime.ProvidableCompositionLocal<android.content.res.Configuration> =
    staticCompositionLocalOf { android.content.res.Configuration() }
