package android.app

import android.content.Context
import android.content.Intent

open class Application : Context() {
    override val packageName: String get() = "me.rerere.rikkahub"
    fun registerActivityLifecycleCallbacks(callbacks: Any) {}
    fun unregisterActivityLifecycleCallbacks(callbacks: Any) {}
}

open class Activity : Context() {
    override val packageName: String get() = "me.rerere.rikkahub"
    val window: Any? get() = null
    var intent: Intent = Intent()
    fun finish() {}
    fun runOnUiThread(action: Runnable) { action.run() }
    fun onBackPressed() {}
    fun recreate() {}
    fun isFinishing(): Boolean = false
    fun requestPermissions(permissions: Array<String>, requestCode: Int) {}
    fun shouldShowRequestPermissionRationale(permission: String): Boolean = false
    fun setResult(resultCode: Int, data: Intent? = null) {}
    fun overridePendingTransition(enter: Int, exit: Int) {}
}

open class Service : Context() {
    override val packageName: String get() = "me.rerere.rikkahub"

    companion object {
        const val START_STICKY = 1
        const val START_NOT_STICKY = 2
        const val START_REDELIVER_INTENT = 3
        const val START_STICKY_COMPATIBILITY = 0
        const val STOP_FOREGROUND_REMOVE = 1
        const val STOP_FOREGROUND_DETACH = 2
    }

    open fun onCreate() {}
    open fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY
    open fun onBind(intent: android.content.Intent?): android.os.IBinder? = null
    open fun onUnbind(intent: android.content.Intent?): Boolean = false
    open fun onDestroy() {}
    open fun onLowMemory() {}

    fun stopSelf() {}
    fun stopSelf(startId: Int) {}
    fun stopSelfResult(startId: Int): Boolean = true
    fun startForeground(id: Int, notification: Notification) {}
    fun startForeground(id: Int, notification: Notification, foregroundServiceType: Int) {}
    fun stopForeground(removeNotification: Boolean) {}
    fun stopForeground(flags: Int) {}
}

class ActivityInfo {
    var name: String? = null
    var packageName: String? = null
}

class ApplicationInfo {
    var packageName: String = "me.rerere.rikkahub"
    var dataDir: String = android.content.AppContext.dataDir.absolutePath
}

class KeyguardManager {
    fun isKeyguardSecure(): Boolean = false
    fun isDeviceSecure(): Boolean = false
    fun isKeyguardLocked(): Boolean = false
}

class PowerManager {
    fun isPowerSaveMode(): Boolean = false
    fun isIgnoringBatteryOptimizations(pkg: String?): Boolean = true
    fun newWakeLock(levelAndFlags: Int, tag: String): WakeLock = WakeLock()
}

class WakeLock {
    fun acquire() {}
    fun acquire(timeout: Long) {}
    fun release() {}
    fun isHeld(): Boolean = false
}

class DownloadManager {
    fun enqueue(request: Request?): Long = 0
    fun remove(ids: LongArray): Int = 0

    class Request(uri: android.net.Uri) {
        fun setDestinationInExternalPublicDir(dirType: String, fileName: String): Request = this
        fun setDestinationUri(uri: android.net.Uri): Request = this
        fun setTitle(title: CharSequence?): Request = this
        fun setDescription(description: CharSequence?): Request = this
        fun setNotificationVisibility(visibility: Int): Request = this
        fun setMimeType(mime: String?): Request = this
        fun addRequestHeader(header: String, value: String): Request = this
    }

    companion object {
        const val VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 3
        const val VISIBILITY_VISIBLE = 0
        const val VISIBILITY_HIDDEN = 2
        const val COLUMN_ID = "_id"
        const val COLUMN_STATUS = "status"
        const val COLUMN_REASON = "reason"
        const val STATUS_SUCCESSFUL = 8
        const val STATUS_FAILED = 16
        const val ERROR_INSUFFICIENT_SPACE = 1006
    }
}