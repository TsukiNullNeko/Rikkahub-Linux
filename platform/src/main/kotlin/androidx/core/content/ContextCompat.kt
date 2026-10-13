package androidx.core.content

import android.content.Context

object ContextCompat {
    fun checkSelfPermission(context: Context, permission: String): Int =
        android.content.pm.PackageManager.PERMISSION_GRANTED

    fun getDrawable(context: Context, id: Int): Any? = null

    fun getSystemService(context: Context, serviceClass: Class<*>): Any? =
        context.getSystemService(serviceName(serviceClass))

    fun startForegroundService(context: Context, intent: android.content.Intent) {}

    fun registerReceiver(context: Context, receiver: Any?, filter: Any?): Any? = null

    fun unregisterReceiver(context: Context, receiver: Any?) {}

    private fun serviceName(cls: Class<*>): String = when (cls.simpleName) {
        "ClipboardManager" -> Context.CLIPBOARD_SERVICE
        "NotificationManager" -> Context.NOTIFICATION_SERVICE
        "ConnectivityManager" -> Context.CONNECTIVITY_SERVICE
        "AudioManager" -> Context.AUDIO_SERVICE
        "InputMethodManager" -> Context.INPUT_METHOD_SERVICE
        "DownloadManager" -> Context.DOWNLOAD_SERVICE
        else -> cls.simpleName
    }
}

fun Context.getSystemServiceName(cls: Class<*>): String = cls.simpleName

inline fun android.content.SharedPreferences.edit(commit: Boolean = false, action: android.content.SharedPreferences.Editor.() -> Unit) {
    val editor = edit()
    editor.action()
    if (commit) editor.commit() else editor.apply()
}