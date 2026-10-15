package android.text.format

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    const val FORMAT_SHOW_TIME = 1
    const val FORMAT_SHOW_DATE = 2
    const val FORMAT_SHOW_WEEKDAY = 4
    const val FORMAT_ABBREV_MONTH = 8
    const val FORMAT_ABBREV_WEEKDAY = 16
    const val FORMAT_24HOUR = 32
    const val FORMAT_ABBREV_ALL = 0x00080000
    const val MINUTE_IN_MILLIS = 60_000L
    const val HOUR_IN_MILLIS = 3_600_000L
    const val DAY_IN_MILLIS = 86_400_000L
    const val WEEK_IN_MILLIS = 7 * DAY_IN_MILLIS
    const val YEAR_IN_MILLIS = 365 * DAY_IN_MILLIS

    fun formatDateTime(context: Any?, millis: Long, flags: Int): String {
        val d = Date(millis)
        val hasTime = flags and FORMAT_SHOW_TIME != 0
        val hasDate = flags and FORMAT_SHOW_DATE != 0
        val pattern = when {
            hasTime && hasDate -> "MMM d, HH:mm"
            hasTime -> "HH:mm"
            else -> "MMM d, yyyy"
        }
        return SimpleDateFormat(pattern, Locale.getDefault()).format(d)
    }

    fun formatElapsedTime(elapsedSeconds: Long): String {
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        else String.format(Locale.US, "%02d:%02d", m, s)
    }

    fun getRelativeTimeSpanString(millis: Long): CharSequence {
        val diff = System.currentTimeMillis() - millis
        val cal = Calendar.getInstance()
        val minutes = diff / MINUTE_IN_MILLIS
        val hours = diff / HOUR_IN_MILLIS
        val days = diff / DAY_IN_MILLIS
        return when {
            diff < 45_000 -> "just now"
            minutes < 60 -> minutes.toString() + " min ago"
            hours < 24 -> hours.toString() + " hr ago"
            days < 7 -> days.toString() + " d ago"
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
        }
    }

    fun getRelativeTimeSpanString(context: Any?, millis: Long, flags: Int): CharSequence =
        getRelativeTimeSpanString(millis)

    fun getRelativeTimeSpanString(millis: Long, now: Long, minResolution: Long): CharSequence =
        getRelativeTimeSpanString(millis)

    fun getRelativeTimeSpanString(millis: Long, now: Long, minResolution: Int): CharSequence =
        getRelativeTimeSpanString(millis)

    fun getRelativeTimeSpanString(millis: Long, now: Long, minResolution: Long, flags: Int): CharSequence =
        getRelativeTimeSpanString(millis)

    fun isToday(millis: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = millis
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}