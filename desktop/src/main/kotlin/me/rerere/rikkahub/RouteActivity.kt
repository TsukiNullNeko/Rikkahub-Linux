package me.rerere.rikkahub

import android.app.Activity
import android.content.Intent

/**
 * Desktop stand-in for the Android RouteActivity.
 *
 * The Linux app hosts [AppRoutes] in the main Compose window; this class only exists so
 * notification intents and context casts keep working.
 */
class RouteActivity : Activity() {
    companion object {
        const val ACTION_TRANSLATE = "me.rerere.rikkahub.action.TRANSLATE"
        const val ACTION_MEDIA_CREATION = "me.rerere.rikkahub.action.MEDIA_CREATION"

        /** Pending navigation requests raised from notifications or deep links. */
        @Volatile
        var pendingDestination: Screen? = null

        fun navigate(screen: Screen) {
            pendingDestination = screen
        }
    }

}