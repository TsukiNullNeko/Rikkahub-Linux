package android.provider

import android.content.ContentResolver
import android.net.Uri

object MediaStore {
    const val AUTHORITY = "media"
    val EXTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/external")
    val INTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/internal")

    object MediaColumns {
        const val _ID = "_id"
        const val DATA = "_data"
        const val DISPLAY_NAME = "_display_name"
        const val SIZE = "_size"
        const val MIME_TYPE = "mime_type"
        const val DATE_ADDED = "date_added"
        const val DATE_MODIFIED = "date_modified"
        const val WIDTH = "width"
        const val HEIGHT = "height"
        const val DURATION = "duration"
        const val RELATIVE_PATH = "relative_path"
    }

    object Images {
        const val MEDIA_TYPE = "media_type"
        object Media {
            val EXTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/external/images/media")
            val INTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/internal/images/media")
        }
        const val MEDIA_TYPE_IMAGE = 1
        const val _ID = MediaColumns._ID
        const val DATA = MediaColumns.DATA
        const val DISPLAY_NAME = MediaColumns.DISPLAY_NAME
        const val SIZE = MediaColumns.SIZE
        const val MIME_TYPE = MediaColumns.MIME_TYPE
        const val DATE_ADDED = MediaColumns.DATE_ADDED
        const val DATE_MODIFIED = MediaColumns.DATE_MODIFIED
        const val WIDTH = MediaColumns.WIDTH
        const val HEIGHT = MediaColumns.HEIGHT
        const val DURATION = MediaColumns.DURATION
        const val RELATIVE_PATH = MediaColumns.RELATIVE_PATH
        val EXTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/external/images/media")
    }

    object Video {
        object Media {
            val EXTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/external/video/media")
            val INTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/internal/video/media")
        }
        val EXTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/external/video/media")
        const val MEDIA_TYPE_VIDEO = 3
    }

    object Audio {
        object Media {
            val EXTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/external/audio/media")
        }
        val EXTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/external/audio/media")
        const val MEDIA_TYPE_AUDIO = 2
    }

    object Files {
        val EXTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/external/file")
    }

    /** Alias used by some call sites (MediaStore.Media.COLUMN_*). */
    object Media {
        const val COLUMN_ID = MediaColumns._ID
        const val COLUMN_DATA = MediaColumns.DATA
        const val COLUMN_DISPLAY_NAME = MediaColumns.DISPLAY_NAME
        const val COLUMN_SIZE = MediaColumns.SIZE
        const val COLUMN_MIME_TYPE = MediaColumns.MIME_TYPE
    }

    object Downloads {
        val EXTERNAL_CONTENT_URI: Uri = Uri.parse("content://media/external/downloads")
        const val IS_PENDING = "is_pending"
    }
}

object OpenableColumns {
    const val DISPLAY_NAME = "_display_name"
    const val SIZE = "_size"
}

object DocumentsContract {
    const val EXTRA_INITIAL_URI = "android.provider.extra.INITIAL_URI"
    const val EXTRA_TITLE = "android.provider.extra.TITLE"
    const val EXTRA_MIME_TYPES = "android.provider.extra.MIME_TYPES"
    const val EXTRA_SHOW_ADVANCED = "android.provider.extra.SHOW_ADVANCED"
    const val EXTRA_ORIENTATION = "android.provider.extra.ORIENTATION"

    object Document {
        const val COLUMN_DISPLAY_NAME = "display_name"
        const val COLUMN_MIME_TYPE = "mime_type"
        const val COLUMN_SIZE = "size"
        const val COLUMN_LAST_MODIFIED = "last_modified"
        const val COLUMN_DOCUMENT_ID = "document_id"
        const val MIME_TYPE_DIR = "vnd.android.document/directory"
    }

    object Root {
        const val COLUMN_ROOT_ID = "root_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DOCUMENT_ID = "document_id"
    }

    fun buildDocumentUri(authority: String, documentId: String): Uri =
        Uri.parse("content://" + authority + "/document/" + documentId)

    fun buildTreeDocumentUri(authority: String, documentId: String): Uri =
        Uri.parse("content://" + authority + "/tree/" + documentId)

    fun getDocumentId(uri: Uri): String = uri.lastPathSegment ?: ""

    fun getTreeDocumentId(uri: Uri): String = uri.lastPathSegment ?: ""

    fun isDocumentUri(context: Any?, uri: Uri): Boolean = uri.pathSegments.contains("document")

    fun buildDocumentUriUsingTree(treeUri: Uri, documentId: String): Uri =
        Uri.parse(treeUri.toString().substringBefore("/tree/") + "/tree/" + (treeUri.lastPathSegment ?: "") + "/document/" + documentId)

    fun buildChildDocumentsUriUsingTree(treeUri: Uri, parentDocumentId: String): Uri =
        Uri.parse(treeUri.toString() + "/document/" + parentDocumentId + "/children")

    fun createDocument(contentResolver: ContentResolver?, parentDocumentUri: Uri, mimeType: String, displayName: String): Uri =
        Uri.parse(parentDocumentUri.toString() + "/document/" + displayName)

    fun deleteDocument(contentResolver: ContentResolver?, documentUri: Uri): Boolean = false

    fun renameDocument(contentResolver: ContentResolver?, documentUri: Uri, displayName: String): Uri = documentUri

    fun getDocumentThumbnail(contentResolver: ContentResolver?, documentUri: Uri, size: android.graphics.Rect?, signal: Any?): Any? = null
}

object CalendarContract {
    val CONTENT_URI: Uri = Uri.parse("content://com.android.calendar/calendars")
    val EVENTS_CONTENT_URI: Uri = Uri.parse("content://com.android.calendar/events")
    val CALENDARS_URI: Uri = Uri.parse("content://com.android.calendar/calendars")
    val INSTANCES_URI: Uri = Uri.parse("content://com.android.calendar/instances/when")
    const val CALLER_IS_SYNCADAPTER = "caller_is_syncadapter"
    const val ACTION_EVENT_REMINDER = "android.intent.action.EVENT_REMINDER"

    object CalendarColumns {
        const val CALENDAR_ID = "_id"
        const val CALENDAR_DISPLAY_NAME = "calendar_displayName"
        const val CALENDAR_COLOR = "calendar_color"
        const val CALENDAR_ACCESS_LEVEL = "calendar_access_level"
        const val VISIBLE = "visible"
        const val IS_PRIMARY = "isPrimary"
        const val ACCOUNT_NAME = "account_name"
        const val ACCOUNT_TYPE = "account_type"
        const val OWNER_ACCOUNT = "ownerAccount"
        const val CALENDAR_TIME_ZONE = "calendar_timezone"
        const val SYNC_EVENTS = "sync_events"
        const val CAL_SYNC1 = "cal_sync1"
        const val CAL_SYNC2 = "cal_sync2"
    }

    object CalendarContractColumns
    object EventsColumns {
        const val _ID = "_id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val EVENT_LOCATION = "eventLocation"
        const val DTSTART = "dtstart"
        const val DTEND = "dtend"
        const val DURATION = "duration"
        const val ALL_DAY = "allDay"
        const val EVENT_TIMEZONE = "eventTimezone"
        const val CALENDAR_ID = "calendar_id"
        const val HAS_ALARM = "hasAlarm"
        const val STATUS = "eventStatus"
        const val DELETED = "deleted"
        const val RRULE = "rrule"
        const val EVENT_END_TIMEZONE = "eventEndTimezone"
        const val STATUS_TENTATIVE = 0
        const val STATUS_CONFIRMED = 1
        const val STATUS_CANCELED = 2
    }

    object InstancesColumns {
        const val _ID = "_id"
        const val EVENT_ID = "event_id"
        const val BEGIN = "begin"
        const val END = "end"
        const val TITLE = "title"
        const val DISPLAY_COLOR = "displayColor"
        const val ALL_DAY = "allDay"
        const val CALENDAR_ID = "calendar_id"
    }

    object CalendarSyncColumns

    object AttendeesColumns

    object RemindersColumns

    /** Calendar rows (content://com.android.calendar/calendars) */
    object Calendars {
        const val _ID = "_id"
        const val CALENDAR_DISPLAY_NAME = "calendar_displayName"
        const val CALENDAR_COLOR = "calendar_color"
        const val CALENDAR_ACCESS_LEVEL = "calendar_access_level"
        const val VISIBLE = "visible"
        const val IS_PRIMARY = "isPrimary"
        const val ACCOUNT_NAME = "account_name"
        const val ACCOUNT_TYPE = "account_type"
        const val OWNER_ACCOUNT = "ownerAccount"
        const val CALENDAR_TIME_ZONE = "calendar_timezone"
        const val SYNC_EVENTS = "sync_events"
        const val CALENDAR_LOCATION = "calendar_location"
        const val DELETED = "deleted"
        const val CAL_SYNC1 = "cal_sync1"
        const val CAL_SYNC2 = "cal_sync2"
        const val CAL_ACCESS_CONTRIBUTOR = 500
        const val CAL_ACCESS_EDITOR = 600
        const val CAL_ACCESS_OWNER = 700
        const val CAL_ACCESS_READ = 200
        const val CAL_ACCESS_RESPOND = 300
        const val CAL_ACCESS_FREEBUSY = 100
        const val CAL_ACCESS_NONE = 0
        val CONTENT_URI: Uri = Uri.parse("content://com.android.calendar/calendars")
    }

    /** Event rows (content://com.android.calendar/events) */
    object Events {
        const val _ID = "_id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val EVENT_LOCATION = "eventLocation"
        const val DTSTART = "dtstart"
        const val DTEND = "dtend"
        const val DURATION = "duration"
        const val ALL_DAY = "allDay"
        const val EVENT_TIMEZONE = "eventTimezone"
        const val EVENT_END_TIMEZONE = "eventEndTimezone"
        const val CALENDAR_ID = "calendar_id"
        const val HAS_ALARM = "hasAlarm"
        const val STATUS = "eventStatus"
        const val DELETED = "deleted"
        const val RRULE = "rrule"
        const val ORGANIZER = "organizer"
        const val EVENT_COLOR = "eventColor"
        const val STATUS_TENTATIVE = 0
        const val STATUS_CONFIRMED = 1
        const val STATUS_CANCELED = 2
        val CONTENT_URI: Uri = Uri.parse("content://com.android.calendar/events")
    }

    /** Instance rows (content://com.android.calendar/instances/when) */
    object Instances {
        const val _ID = "_id"
        const val EVENT_ID = "event_id"
        const val BEGIN = "begin"
        const val END = "end"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val EVENT_LOCATION = "eventLocation"
        const val DISPLAY_COLOR = "displayColor"
        const val ALL_DAY = "allDay"
        const val CALENDAR_ID = "calendar_id"
        const val CALENDAR_DISPLAY_NAME = "calendar_displayName"
        const val EVENT_COLOR = "eventColor"
        const val START_MINUTE = "startMinute"
        const val END_MINUTE = "endMinute"
        const val START_DAY = "startDay"
        const val END_DAY = "endDay"
        const val RRULE = "rrule"
        val CONTENT_URI: Uri = Uri.parse("content://com.android.calendar/instances/when")
        val CONTENT_BY_DAY_URI: Uri = Uri.parse("content://com.android.calendar/instances/whenbyday")
        val CONTENT_SEARCH_URI: Uri = Uri.parse("content://com.android.calendar/instances/search")

        fun query(uri: Uri, projection: Array<String>?): android.database.Cursor? = null
    }
}

object Settings {
    const val ACTION_SETTINGS = "android.settings.SETTINGS"
    const val ACTION_APPLICATION_DETAILS_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS"
    const val ACTION_MANAGE_OVERLAY_PERMISSION = "android.settings.action.MANAGE_OVERLAY_PERMISSION"
    const val ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"
    const val ACTION_USAGE_ACCESS_SETTINGS = "android.settings.USAGE_ACCESS_SETTINGS"
    const val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    const val EXTRA_APP_PACKAGE = "android.provider.extra.APP_PACKAGE"

    object System {
        const val CAN_WRITE = "can_write"
    }

    object Secure {
        const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
        const val ACCESSIBILITY_ENABLED = "accessibility_enabled"
    }
}