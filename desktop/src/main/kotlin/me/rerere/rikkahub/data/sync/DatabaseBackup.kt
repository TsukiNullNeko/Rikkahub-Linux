package me.rerere.rikkahub.data.sync

import java.io.File
import java.sql.DriverManager

/**
 * Database snapshot helpers for backup/restore, implemented over JDBC (sqlite-jdbc).
 */
internal object DatabaseBackup {
    const val ARCHIVE_DATABASE = "rikka_hub.db"
    const val WAL = "rikka_hub-wal"
    const val SHM = "rikka_hub-shm"

    /** VACUUM INTO includes committed WAL contents in a consistent, standalone snapshot. */
    fun createSnapshot(databasePath: String, destination: File) {
        check(!destination.exists()) { "Backup snapshot already exists" }
        connect(databasePath).use { connection ->
            connection.createStatement().use { statement ->
                val escaped = destination.absolutePath.replace("'", "''")
                statement.execute("VACUUM main INTO '$escaped'")
            }
        }
    }

    /** Only opens the staged copy. The caller must place its matching WAL beside it first. */
    fun normalize(context: android.content.Context, databaseFile: File) {
        require(databaseFile.isFile && databaseFile.length() > 0) { "Backup database is missing or empty" }
        connect(databaseFile.absolutePath).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA wal_checkpoint(TRUNCATE)")
                statement.executeQuery("PRAGMA integrity_check").use { rs ->
                    check(rs.next() && rs.getString(1).equals("ok", ignoreCase = true)) {
                        "Backup database failed its integrity check"
                    }
                }
            }
        }
        removeSidecars(databaseFile)
    }

    fun checkpoint(databasePath: String) {
        connect(databasePath).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA wal_checkpoint(TRUNCATE)")
            }
        }
    }

    fun removeSidecars(databaseFile: File) {
        for (suffix in listOf("-wal", "-shm")) {
            val sidecar = File(databaseFile.path + suffix)
            if (sidecar.exists() && !sidecar.delete()) {
                error("Could not remove staged database sidecar")
            }
        }
    }

    private fun connect(path: String): java.sql.Connection =
        DriverManager.getConnection("jdbc:sqlite:" + path)
}
