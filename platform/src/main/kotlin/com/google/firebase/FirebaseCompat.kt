package com.google.firebase

import android.os.Bundle

/** No-op analytics/crashlytics stand-ins (the Linux build has no Firebase). */
object Firebase {
    val analytics: FirebaseAnalytics get() = FirebaseAnalytics
    val crashlytics: FirebaseCrashlytics get() = FirebaseCrashlytics
}

object FirebaseAnalytics {
    fun logEvent(name: String, params: Bundle? = null) {}
    fun setAnalyticsCollectionEnabled(enabled: Boolean) {}
    fun setUserId(id: String?) {}
    fun setUserProperty(name: String, value: String?) {}
}

object FirebaseCrashlytics {
    fun log(message: String) {}
    fun recordException(throwable: Throwable) {}
    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {}
    fun setUserId(identifier: String) {}
}