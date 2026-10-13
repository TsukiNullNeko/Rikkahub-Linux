package android.app

import android.content.Intent

class NotificationChannel(val id: String, val name: CharSequence, val importance: Int) {
    private var descriptionText: String? = null
    private var soundUri: android.net.Uri? = null
    private var vibrationEnabledFlag: Boolean = false
    private var lightsEnabledFlag: Boolean = false
    private var lightColorValue: Int = 0
    private var groupId: String? = null
    private var showBadgeFlag: Boolean = true
    private var vibrationPatternValue: LongArray? = null

    fun getDescription(): String? = descriptionText
    fun getSound(): android.net.Uri? = soundUri
    fun shouldVibrate(): Boolean = vibrationEnabledFlag
    fun shouldShowLights(): Boolean = lightsEnabledFlag
    fun getLightColor(): Int = lightColorValue
    fun getGroup(): String? = groupId
    fun canShowBadge(): Boolean = showBadgeFlag
    fun getVibrationPattern(): LongArray? = vibrationPatternValue

    fun setSound(sound: android.net.Uri?, attrs: Any?) { soundUri = sound }
    fun setShowBadge(show: Boolean) { showBadgeFlag = show }
    fun enableVibration(vibrate: Boolean) { vibrationEnabledFlag = vibrate }
    fun enableLights(lights: Boolean) { lightsEnabledFlag = lights }
    fun setLightColor(color: Int) { lightColorValue = color }
    fun setDescription(description: String?) { descriptionText = description }
    fun setGroup(group: String?) { groupId = group }
    fun setVibrationPattern(pattern: LongArray?) { vibrationPatternValue = pattern }

    companion object {
        const val IMPORTANCE_DEFAULT = 3
        const val IMPORTANCE_HIGH = 4
        const val IMPORTANCE_LOW = 2
        const val IMPORTANCE_MIN = 1
        const val IMPORTANCE_NONE = 0
    }
}

class NotificationChannelGroup(val id: String, name: CharSequence) {
    var description: String? = null
}

class Notification {
    var title: CharSequence? = null
    var text: CharSequence? = null
    var iconRes: Int? = null
    var autoCancel: Boolean = true
    var ongoing: Boolean = false
    var silent: Boolean = false
    var onlyAlertOnce: Boolean = false
    val contentIntent: PendingIntent? = null
    var extras: android.os.Bundle = android.os.Bundle()

    class Builder(val channelId: String) {
        private val n = Notification()
        fun setContentTitle(title: CharSequence?): Builder { n.title = title; return this }
        fun setContentText(text: CharSequence?): Builder { n.text = text; return this }
        fun setSmallIcon(icon: Int): Builder { n.iconRes = icon; return this }
        fun setAutoCancel(cancel: Boolean): Builder { n.autoCancel = cancel; return this }
        fun setContentIntent(intent: PendingIntent?): Builder { return this }
        fun setOngoing(ongoing: Boolean): Builder { n.ongoing = ongoing; return this }
        fun setProgress(max: Int, progress: Int, indeterminate: Boolean): Builder { return this }
        fun setOnlyAlertOnce(once: Boolean): Builder { n.onlyAlertOnce = once; return this }
        fun setSilent(silent: Boolean): Builder { n.silent = silent; return this }
        fun setWhen(whenTime: Long): Builder { return this }
        fun setPriority(priority: Int): Builder { return this }
        fun addAction(action: Any?): Builder { return this }
        fun setStyle(style: Any?): Builder { return this }
        fun setExtras(extras: android.os.Bundle): Builder { n.extras = extras; return this }
        fun build(): Notification = n
    }
}

class NotificationManager {
    fun createNotificationChannel(channel: NotificationChannel) {
        android.util.Log.d("Notification", "channel: " + channel.id)
    }

    fun createNotificationChannels(channels: List<NotificationChannel>) {
        channels.forEach { createNotificationChannel(it) }
    }

    fun createNotificationChannelGroup(group: NotificationChannelGroup) {}
    fun deleteNotificationChannel(channelId: String) {}
    fun getNotificationChannel(channelId: String): NotificationChannel? = null
    fun getNotificationChannels(): List<NotificationChannel> = emptyList()

    fun notify(id: Int, notification: Notification) {
        val text = notification.text?.toString() ?: ""
        val title = notification.title?.toString() ?: "RikkaHub"
        android.content.AppContext.toastHandler?.invoke(
            android.content.AppContext.ToastData(title + "\n" + text, 1)
        )
        // Desktop notification via notify-send when available
        try {
            val args = mutableListOf("notify-send", "-a", "RikkaHub", title)
            if (text.isNotEmpty()) args.add(text)
            ProcessBuilder(args).start()
        } catch (_: Exception) {
        }
    }

    fun cancel(id: Int) {}
    fun cancel(tag: String?, id: Int) {}
    fun cancelAll() {}
    fun areNotificationsEnabled(): Boolean = true
    fun isNotificationChannelEnabled(channelId: String): Boolean = true

    companion object {
        const val IMPORTANCE_DEFAULT = 3
        const val IMPORTANCE_HIGH = 4
        const val IMPORTANCE_LOW = 2
        const val IMPORTANCE_MIN = 1
        const val IMPORTANCE_NONE = 0
        const val VISIBILITY_PUBLIC = 1
        const val VISIBILITY_PRIVATE = 0
        const val VISIBILITY_SECRET = -1
    }
}

class PendingIntent {
    fun send() {}
    fun send(context: android.content.Context?, code: Int, intent: Intent?) {}
    fun cancel() {}

    companion object {
        const val FLAG_UPDATE_CURRENT = 1 shl 27
        const val FLAG_IMMUTABLE = 1 shl 26
        const val FLAG_MUTABLE = 1 shl 25
        const val FLAG_CANCEL_CURRENT = 1 shl 28
        const val FLAG_NO_CREATE = 1 shl 29
        const val FLAG_ONE_SHOT = 1 shl 30

        fun getActivity(context: android.content.Context?, requestCode: Int, intent: Intent?, flags: Int): PendingIntent = PendingIntent()
        fun getBroadcast(context: android.content.Context?, requestCode: Int, intent: Intent?, flags: Int): PendingIntent = PendingIntent()
        fun getService(context: android.content.Context?, requestCode: Int, intent: Intent?, flags: Int): PendingIntent = PendingIntent()
    }
}