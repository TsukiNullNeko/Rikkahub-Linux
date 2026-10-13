package androidx.core.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context

class NotificationManagerCompat private constructor(private val context: Context) {
    fun notify(id: Int, notification: Notification) {
        android.content.AppContext.notificationManager.notify(id, notification)
    }

    fun notify(tag: String?, id: Int, notification: Notification) = notify(id, notification)

    fun cancel(id: Int) {
        android.content.AppContext.notificationManager.cancel(id)
    }

    fun cancel(tag: String?, id: Int) = cancel(id)

    fun cancelAll() {
        android.content.AppContext.notificationManager.cancelAll()
    }

    fun areNotificationsEnabled(): Boolean = true

    companion object {
        const val IMPORTANCE_DEFAULT = 3
        const val IMPORTANCE_HIGH = 4
        const val IMPORTANCE_LOW = 2
        const val IMPORTANCE_MIN = 1
        const val IMPORTANCE_NONE = 0

        @JvmStatic
        fun from(context: Context): NotificationManagerCompat = NotificationManagerCompat(context)

        @JvmStatic
        fun areNotificationsEnabled(context: Context): Boolean = true
    }
}

class NotificationChannelCompat private constructor(
    val id: String,
    val name: CharSequence,
    val importance: Int
) {
    class Builder(val id: String, val importance: Int) {
        private var name: CharSequence = id
        private var description: String? = null
        private var showBadge: Boolean = true
        private var sound: android.net.Uri? = null
        private var vibrationEnabled: Boolean = false

        fun setName(name: CharSequence?): Builder {
            if (name != null) this.name = name
            return this
        }

        fun setDescription(description: String?): Builder {
            this.description = description
            return this
        }

        fun setShowBadge(showBadge: Boolean): Builder {
            this.showBadge = showBadge
            return this
        }

        fun setSound(sound: android.net.Uri?, audioAttributes: Any?): Builder {
            this.sound = sound
            return this
        }

        fun setVibrationEnabled(enabled: Boolean): Builder {
            this.vibrationEnabled = enabled
            return this
        }

        fun build(): NotificationChannelCompat = NotificationChannelCompat(id, name, importance)
    }
}

object NotificationCompat {
    const val PRIORITY_DEFAULT = 0
    const val PRIORITY_HIGH = 1
    const val PRIORITY_LOW = -1
    const val PRIORITY_MIN = -2
    const val VISIBILITY_PUBLIC = 1
    const val VISIBILITY_PRIVATE = 0
    const val VISIBILITY_SECRET = -1
    const val CATEGORY_PROGRESS = "progress"
    const val CATEGORY_MESSAGE = "msg"
    const val CATEGORY_SERVICE = "service"
    const val DEFAULT_ALL = -1
    const val DEFAULT_SOUND = 1
    const val DEFAULT_VIBRATE = 2
    const val DEFAULT_LIGHTS = 4

    /** Minimal stand-in for the big-text notification style. */
    class BigTextStyle {
        private var bigText: CharSequence? = null

        fun setBigContentTitle(title: CharSequence?): BigTextStyle = this
        fun setSummaryText(summary: CharSequence?): BigTextStyle = this
        fun bigText(text: CharSequence?): BigTextStyle {
            bigText = text
            return this
        }

        fun getBigText(): CharSequence? = bigText
    }

    class BigPictureStyle {
        fun setBigContentTitle(title: CharSequence?): BigPictureStyle = this
        fun setSummaryText(summary: CharSequence?): BigPictureStyle = this
    }

    class Builder(private val context: Context, private val channelId: String) {
        private val builder = Notification.Builder(channelId)

        fun setContentTitle(title: CharSequence?): Builder {
            builder.setContentTitle(title)
            return this
        }

        fun setContentText(text: CharSequence?): Builder {
            builder.setContentText(text)
            return this
        }

        fun setSmallIcon(icon: Int): Builder {
            builder.setSmallIcon(icon)
            return this
        }

        fun setContentIntent(intent: PendingIntent?): Builder {
            builder.setContentIntent(intent)
            return this
        }

        fun setAutoCancel(cancel: Boolean): Builder {
            builder.setAutoCancel(cancel)
            return this
        }

        fun setOngoing(ongoing: Boolean): Builder {
            builder.setOngoing(ongoing)
            return this
        }

        fun setOnlyAlertOnce(once: Boolean): Builder {
            builder.setOnlyAlertOnce(once)
            return this
        }

        fun setSilent(silent: Boolean): Builder {
            builder.setSilent(silent)
            return this
        }

        fun setPriority(priority: Int): Builder = this
        fun setVisibility(visibility: Int): Builder = this
        fun setCategory(category: String?): Builder = this
        fun setProgress(max: Int, progress: Int, indeterminate: Boolean): Builder {
            builder.setProgress(max, progress, indeterminate)
            return this
        }

        fun setForegroundServiceBehavior(behavior: Int): Builder = this
        fun setShowWhen(show: Boolean): Builder = this
        fun setWhen(whenTime: Long): Builder = this
        fun setStyle(style: Any?): Builder = this
        fun addAction(icon: Int, title: CharSequence?, intent: PendingIntent?): Builder = this
        fun setGroup(group: String?): Builder = this
        fun setSubText(text: CharSequence?): Builder = this
        fun setRequestPromotedOngoing(requestPromotedOngoing: Boolean): Builder = this
        fun setShortCriticalText(shortCriticalText: String?): Builder = this
        fun setContentInfo(info: CharSequence?): Builder = this
        fun setNumber(number: Int): Builder = this
        fun setLocalOnly(localOnly: Boolean): Builder = this
        fun setVibrate(pattern: LongArray?): Builder = this
        fun setLights(argb: Int, onMs: Int, offMs: Int): Builder = this
        fun setColor(color: Int): Builder = this
        fun setTicker(ticker: CharSequence?): Builder = this
        fun setDeleteIntent(intent: PendingIntent?): Builder = this
        fun setDefaults(defaults: Int): Builder = this

        fun build(): Notification = builder.build()
    }
}

object ServiceCompat {
    const val START_STICKY = 1
    const val START_NOT_STICKY = 2
    const val START_REDELIVER_INTENT = 3
    const val STOP_FOREGROUND_REMOVE = 1
    const val STOP_FOREGROUND_DETACH = 2

    fun startForeground(service: Any?, id: Int, notification: Notification, foregroundServiceType: Int = 0) {}
    fun stopForeground(service: Any?, flags: Int) {}
}