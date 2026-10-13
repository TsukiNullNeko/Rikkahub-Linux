package androidx.browser.customtabs

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Desktop stand-in for Android Custom Tabs: opens the URL in the system browser.
 */
class CustomTabsIntent private constructor(val intent: Intent) {
    class Builder {
        private var colorScheme: Int = 0

        fun setShowTitle(showTitle: Boolean): Builder = this
        fun setToolbarColor(color: Int): Builder = this
        fun setSecondaryToolbarColor(color: Int): Builder = this
        fun setNavigationBarColor(color: Int): Builder = this
        fun setColorScheme(scheme: Int): Builder {
            colorScheme = scheme
            return this
        }
        fun setUrlBarHidingEnabled(enabled: Boolean): Builder = this
        fun setShowTitleEnabled(show: Boolean): Builder = this
        fun addDefaultShareMenuItem(): Builder = this
        fun setInstantAppsEnabled(enabled: Boolean): Builder = this
        fun setStartAnimations(context: Context, enterResId: Int, exitResId: Int): Builder = this
        fun setExitAnimations(context: Context, enterResId: Int, exitResId: Int): Builder = this
        fun enableUrlBarHiding(): Builder = this
        fun build(): CustomTabsIntent = CustomTabsIntent(Intent(Intent.ACTION_VIEW))
    }

    fun launchUrl(context: Context, uri: Uri) {
        android.content.AppContext.openExternally(uri)
    }
}
