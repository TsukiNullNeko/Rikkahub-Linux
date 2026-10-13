package android.content

import android.database.Cursor
import android.net.Uri

abstract class ContentProvider {
    open fun onCreate(): Boolean = true
    open fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    open fun getType(uri: Uri): String? = null
    open fun insert(uri: Uri, values: ContentValues?): Uri? = null
    open fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    open fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
    open fun getContext(): Context = AppContext.appContext
}
