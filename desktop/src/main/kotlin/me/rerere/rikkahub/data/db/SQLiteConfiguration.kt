package me.rerere.rikkahub.data.db

import android.content.AppContext
import java.io.File

/** Desktop database locations (Room's multiplatform SQLite driver). */
internal object SQLiteConfiguration {
    const val DATABASE_NAME = "rikka_hub"

    private val databasesDir: File
        get() = File(AppContext.dataDir, "databases").also { it.mkdirs() }

    fun databaseFile(name: String = DATABASE_NAME): File = File(databasesDir, "$name.db")

    fun databasePath(name: String = DATABASE_NAME): String = databaseFile(name).absolutePath
}
