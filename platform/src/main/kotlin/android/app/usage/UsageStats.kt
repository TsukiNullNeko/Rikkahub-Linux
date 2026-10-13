package android.app.usage

/**
 * Desktop stand-in for Android's usage-stats APIs. Linux has no equivalent of per-app
 * screen-time tracking, so all queries return empty data.
 */
class UsageStatsManager {
    companion object {
        const val INTERVAL_DAILY = 0
        const val INTERVAL_WEEKLY = 1
        const val INTERVAL_MONTHLY = 2
        const val INTERVAL_YEARLY = 3
        const val INTERVAL_BEST = 4
    }

    fun queryUsageStats(intervalType: Int, beginTime: Long, endTime: Long): List<UsageStats> = emptyList()

    fun queryAndAggregateUsageStats(beginTime: Long, endTime: Long): Map<String, UsageStats> = emptyMap()

    fun queryEvents(beginTime: Long, endTime: Long): UsageEvents = UsageEvents()

    fun queryConfigurations(intervalType: Int, beginTime: Long, endTime: Long): List<Any> = emptyList()

    fun isAppInactive(packageName: String): Boolean = false

    fun getAppStandbyBucket(): Int = 10
}

class UsageStats {
    var packageName: String = ""
    var totalTimeInForeground: Long = 0
    var totalTimeVisible: Long = 0
    var lastTimeUsed: Long = 0
    var firstTimeStamp: Long = 0
    var lastTimeStamp: Long = 0
}

class UsageEvents {
    class Event {
        var packageName: String? = null
        var className: String? = null
        var eventType: Int = 0
        var timeStamp: Long = 0

        companion object {
            const val NONE = 0
            const val MOVE_TO_FOREGROUND = 1
            const val MOVE_TO_BACKGROUND = 2
            const val ACTIVITY_RESUMED = 1
            const val ACTIVITY_PAUSED = 2
            const val ACTIVITY_STOPPED = 23
            const val SCREEN_INTERACTIVE = 15
            const val SCREEN_NON_INTERACTIVE = 16
            const val KEYGUARD_SHOWN = 17
            const val KEYGUARD_HIDDEN = 18
        }
    }

    fun hasNextEvent(): Boolean = false

    fun getNextEvent(event: Event): Boolean = false

    companion object {
        const val NONE = 0
        const val MOVE_TO_FOREGROUND = 1
        const val MOVE_TO_BACKGROUND = 2
        const val ACTIVITY_RESUMED = 1
        const val ACTIVITY_PAUSED = 2
        const val ACTIVITY_STOPPED = 23
        const val SCREEN_INTERACTIVE = 15
        const val SCREEN_NON_INTERACTIVE = 16
        const val KEYGUARD_SHOWN = 17
        const val KEYGUARD_HIDDEN = 18
    }
}