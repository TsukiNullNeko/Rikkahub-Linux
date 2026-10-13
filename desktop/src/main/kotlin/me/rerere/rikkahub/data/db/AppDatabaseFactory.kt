package me.rerere.rikkahub.data.db

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import me.rerere.rikkahub.data.db.migrations.Migration_6_7
import me.rerere.rikkahub.data.db.migrations.Migration_11_12
import me.rerere.rikkahub.data.db.migrations.Migration_13_14
import me.rerere.rikkahub.data.db.migrations.Migration_14_15
import me.rerere.rikkahub.data.db.migrations.Migration_15_16
import me.rerere.rikkahub.data.db.migrations.Migration_27_28

private const val TAG = "AppDatabaseFactory"

/** Shared schema, migrations and SQLite setup for the app and staged backup validation. */
internal object AppDatabaseFactory {
    fun create(context: Context, name: String = SQLiteConfiguration.DATABASE_NAME): AppDatabase =
        Room.databaseBuilder<AppDatabase>(name = SQLiteConfiguration.databasePath(name))
            .setDriver(BundledSQLiteDriver())
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(
                Migration_6_7,
                Migration_11_12,
                Migration_13_14,
                Migration_14_15,
                Migration_15_16,
                Migration_27_28,
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(connection: SQLiteConnection) {
                    // FTS5 index for message search. The Android build installs a jieba tokenizer;
                    // the Linux build uses the built-in unicode61 tokenizer instead.
                    try {
                        connection.execSQL(
                            """
                            CREATE VIRTUAL TABLE IF NOT EXISTS message_fts USING fts5(
                                text,
                                node_id UNINDEXED,
                                message_id UNINDEXED,
                                conversation_id UNINDEXED,
                                title UNINDEXED,
                                update_at UNINDEXED,
                                tokenize = 'unicode61'
                            )
                            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "failed to create message_fts", e)
                    }
                }
            })
            .build()
}
