package android.content

import java.io.File

// Minimal placeholder mirroring the android.database.sqlite.SQLiteDatabase surface used by the app.
class SQLiteDatabase private constructor() {
    fun interface CursorFactory {
        fun newCursor(db: SQLiteDatabase?): Cursor?
    }

    fun execSQL(sql: String) {}
    fun close() {}
    fun getPath(): String = ""

    companion object {
        fun openOrCreateDatabase(file: File, factory: CursorFactory?): SQLiteDatabase = SQLiteDatabase()
    }
}
