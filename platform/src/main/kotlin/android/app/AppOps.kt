package android.app

import android.os.Handler

class AppOpsManager {
    companion object {
        const val OPSTR_SYSTEM_ALERT_WINDOW = "android:system_alert_window"
        const val OPSTR_GET_USAGE_STATS = "android:get_usage_stats"
        const val OPSTR_POST_NOTIFICATION = "android:post_notification"
        const val OPSTR_RECORD_AUDIO = "android:record_audio"
        const val MODE_ALLOWED = 0
        const val MODE_IGNORED = 1
        const val MODE_ERRORED = 2
        const val MODE_DEFAULT = 3
        const val OPSTR_MANAGE_EXTERNAL_STORAGE = "android:manage_external_storage"
    }

    fun checkOpNoThrow(op: String, uid: Int, packageName: String): Int = MODE_ALLOWED
    fun checkOpNoThrow(op: Int, uid: Int, packageName: String): Int = MODE_ALLOWED
    fun noteOp(op: String, uid: Int, packageName: String): Int = MODE_ALLOWED
    fun unsafeCheckOpNoThrow(op: String, uid: Int, packageName: String): Int = MODE_ALLOWED
    fun startWatchingMode(op: String, packageName: String?, callback: OnOpChangedListener) {}
    fun stopWatchingMode(callback: OnOpChangedListener) {}

    fun interface OnOpChangedListener {
        fun onOpChanged(op: String?, packageName: String?)
    }
}
