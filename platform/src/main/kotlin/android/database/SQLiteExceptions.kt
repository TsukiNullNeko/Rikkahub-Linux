package android.database.sqlite

open class SQLiteException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

class SQLiteBlobTooBigException(message: String? = null) : SQLiteException(message)

class SQLiteFullException(message: String? = null) : SQLiteException(message)

class SQLiteDiskIOException(message: String? = null) : SQLiteException(message)

class SQLiteDatabaseCorruptException(message: String? = null) : SQLiteException(message)