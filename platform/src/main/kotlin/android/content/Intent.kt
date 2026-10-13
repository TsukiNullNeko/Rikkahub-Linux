package android.content

import android.net.Uri
import android.os.Bundle
import java.io.Serializable

class Intent : Parcelable {
    companion object {
        const val ACTION_VIEW = "android.intent.action.VIEW"
        const val ACTION_SEND = "android.intent.action.SEND"
        const val ACTION_SEND_MULTIPLE = "android.intent.action.SEND_MULTIPLE"
        const val ACTION_SENDTO = "android.intent.action.SENDTO"
        const val ACTION_MAIN = "android.intent.action.MAIN"
        const val ACTION_CHOOSER = "android.intent.action.CHOOSER"
        const val ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT"
        const val ACTION_CREATE_DOCUMENT = "android.intent.action.CREATE_DOCUMENT"
        const val ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT"
        const val ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE"
        const val CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER"
        const val CATEGORY_HOME = "android.intent.category.HOME"
        const val CATEGORY_DEFAULT = "android.intent.category.DEFAULT"
        const val CATEGORY_BROWSABLE = "android.intent.category.BROWSABLE"
        const val CATEGORY_OPENABLE = "android.intent.category.OPENABLE"
        const val FLAG_ACTIVITY_NEW_TASK = 0x10000000
        const val FLAG_ACTIVITY_SINGLE_TOP = 0x20000000
        const val FLAG_ACTIVITY_CLEAR_TOP = 0x04000000
        const val FLAG_ACTIVITY_CLEAR_TASK = 0x00008000
        const val FLAG_ACTIVITY_NO_HISTORY = 0x40000000
        const val FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS = 0x00800000
        const val FLAG_GRANT_READ_URI_PERMISSION = 1
        const val FLAG_GRANT_WRITE_URI_PERMISSION = 2
        const val EXTRA_STREAM = "android.intent.extra.STREAM"
        const val EXTRA_TEXT = "android.intent.extra.TEXT"
        const val EXTRA_SUBJECT = "android.intent.extra.SUBJECT"
        const val EXTRA_EMAIL = "android.intent.extra.EMAIL"
        const val EXTRA_TITLE = "android.intent.extra.TITLE"
        const val EXTRA_MIME_TYPES = "android.intent.extra.MIME_TYPES"
        const val EXTRA_ALLOW_MULTIPLE = "android.intent.extra.ALLOW_MULTIPLE"
        const val EXTRA_INTENT = "android.intent.extra.INTENT"

        @JvmStatic
        fun createChooser(target: Intent, title: CharSequence?): Intent {
            val chooser = Intent(ACTION_CHOOSER)
            chooser.putExtra(EXTRA_INTENT, target)
            chooser.putExtra(EXTRA_TITLE, title?.toString())
            return chooser
        }
    }

    var action: String? = null
    var data: Uri? = null
    var type: String? = null
    private val extras = Bundle()

    constructor()
    constructor(action: String?) { this.action = action }
    constructor(action: String?, uri: Uri?) { this.action = action; this.data = uri }
    constructor(context: Context, cls: Class<*>) { this.action = ACTION_VIEW; extras.putString("class", cls.name) }
    constructor(action: String?, uri: Uri?, context: Context, cls: Class<*>) { this.action = action; this.data = uri }

    fun setAction(action: String?): Intent { this.action = action; return this }
    fun setData(uri: Uri?): Intent { this.data = uri; return this }
    fun setDataAndType(uri: Uri?, type: String?): Intent { this.data = uri; this.type = type; return this }
    fun setType(type: String?): Intent { this.type = type; return this }
    private val categories = mutableSetOf<String>()

    fun addCategory(category: String?): Intent {
        if (category != null) categories.add(category)
        return this
    }

    fun hasCategory(category: String?): Boolean = category != null && categories.contains(category)

    fun addFlags(flags: Int): Intent { return this }
    fun setFlags(flags: Int): Intent { return this }
    fun setPackage(pkg: String?): Intent { return this }
    fun setClassName(pkg: String, cls: String): Intent { return this }

    fun putExtra(name: String, value: String?): Intent { extras.putString(name, value); return this }
    fun putExtra(name: String, value: Int): Intent { extras.putInt(name, value); return this }
    fun putExtra(name: String, value: Long): Intent { extras.putLong(name, value); return this }
    fun putExtra(name: String, value: Boolean): Intent { extras.putBoolean(name, value); return this }
    fun putExtra(name: String, value: Array<String>?): Intent { extras.putStringArray(name, value); return this }
    fun putExtra(name: String, value: Serializable?): Intent { extras.putSerializable(name, value); return this }
    fun putExtra(name: String, value: Parcelable?): Intent { extras.putSerializable(name, value as? Serializable); return this }

    fun hasExtra(name: String): Boolean = extras.containsKey(name)
    fun removeExtra(name: String) { extras.remove(name) }
    fun getStringExtra(name: String): String? = extras.getString(name)
    fun getIntExtra(name: String, def: Int): Int = if (extras.containsKey(name)) extras.getInt(name) else def
    fun getLongExtra(name: String, def: Long): Long = if (extras.containsKey(name)) extras.getLong(name) else def
    fun getBooleanExtra(name: String, def: Boolean): Boolean = if (extras.containsKey(name)) extras.getBoolean(name) else def
    fun getStringArrayExtra(name: String): Array<String>? = extras.getStringArray(name)
    fun getSerializableExtra(name: String): Serializable? = extras.getSerializable(name)
    fun getParcelableExtra(name: String): Parcelable? = extras.getSerializable(name) as? Parcelable
    fun getExtras(): Bundle = extras

    fun resolveType(context: Context): String? = type

    override fun toString(): String {
        return "Intent(action=" + action + ", data=" + data + ")"
    }
}

interface Parcelable