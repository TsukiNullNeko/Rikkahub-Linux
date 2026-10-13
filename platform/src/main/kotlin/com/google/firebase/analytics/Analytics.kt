package com.google.firebase.analytics

import android.os.Bundle
import com.google.firebase.Firebase

/** No-op analytics facade matching the Android API surface used by the app. */
class FirebaseAnalytics {
    fun logEvent(name: String, params: Bundle? = null) {}
    fun setAnalyticsCollectionEnabled(enabled: Boolean) {}
    fun setUserId(id: String?) {}
    fun setUserProperty(name: String, value: String?) {}
    fun setCurrentScreen(activity: Any?, screenName: String?, screenClass: String?) {}

    companion object {
        const val EVENT_SCREEN_VIEW = "screen_view"
        const val PARAM_SCREEN_NAME = "screen_name"
        const val PARAM_SCREEN_CLASS = "screen_class"
    }
}

/** Extension property mirroring com.google.firebase.analytics.analytics. */
val Firebase.analytics: FirebaseAnalytics get() = FirebaseAnalytics()