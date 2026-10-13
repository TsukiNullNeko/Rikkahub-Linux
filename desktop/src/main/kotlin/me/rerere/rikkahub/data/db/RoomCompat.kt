package androidx.room

import kotlinx.coroutines.withContext

/**
 * Room KMP exposes transactions through Transactor; this restores the familiar
 * RoomDatabase.withTransaction { } helper used throughout the app.
 */
suspend fun <R> RoomDatabase.withTransaction(block: suspend () -> R): R =
    useWriterConnection { transactor ->
        transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
            block()
        }
    }
