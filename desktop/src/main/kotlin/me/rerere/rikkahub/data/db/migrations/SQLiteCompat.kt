package me.rerere.rikkahub.data.db.migrations

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement

/**
 * Small compatibility layer that lets the Android-era migrations run on Room's
 * multiplatform SQLiteConnection API.
 */

fun SQLiteConnection.execSQL(sql: String) {
    prepare(sql).use { it.step() }
}

fun SQLiteConnection.execSQL(sql: String, bindArgs: Array<Any?>) {
    prepare(sql).use { statement ->
        bindArgs.forEachIndexed { index, value -> statement.bindValue(index + 1, value) }
        statement.step()
    }
}

fun SQLiteConnection.query(sql: String): CompatCursor = CompatCursor(prepare(sql))

fun SQLiteConnection.query(sql: String, bindArgs: Array<Any?>): CompatCursor {
    val statement = prepare(sql)
    bindArgs.forEachIndexed { index, value -> statement.bindValue(index + 1, value) }
    return CompatCursor(statement)
}

private fun SQLiteStatement.bindValue(index: Int, value: Any?) {
    when (value) {
        null -> bindNull(index)
        is String -> bindText(index, value)
        is Int -> bindLong(index, value.toLong())
        is Long -> bindLong(index, value)
        is Short -> bindLong(index, value.toLong())
        is Byte -> bindLong(index, value.toLong())
        is Boolean -> bindLong(index, if (value) 1L else 0L)
        is Float -> bindDouble(index, value.toDouble())
        is Double -> bindDouble(index, value)
        is ByteArray -> bindBlob(index, value)
        else -> bindText(index, value.toString())
    }
}

class CompatCursor(private val statement: SQLiteStatement) : AutoCloseable {
    private var started = false

    fun moveToFirst(): Boolean {
        if (started) return false
        started = true
        return statement.step()
    }

    fun moveToNext(): Boolean {
        if (!started) return moveToFirst()
        return statement.step()
    }

    fun moveToPosition(position: Int): Boolean {
        var index = 0
        while (index <= position) {
            val hasRow = if (!started) moveToFirst() else statement.step()
            if (!hasRow) return false
            if (index == position) return true
            index++
        }
        return false
    }

    fun getColumnCount(): Int = statement.getColumnCount()

    fun getString(index: Int): String? = if (statement.isNull(index)) null else statement.getText(index)

    fun getLong(index: Int): Long = statement.getLong(index)

    fun getInt(index: Int): Int = statement.getInt(index)

    fun getBlob(index: Int): ByteArray? = if (statement.isNull(index)) null else statement.getBlob(index)

    fun isNull(index: Int): Boolean = statement.isNull(index)

    fun getColumnIndex(name: String): Int {
        for (i in 0 until statement.getColumnCount()) {
            if (statement.getColumnName(i).equals(name, ignoreCase = true)) return i
        }
        return -1
    }

    override fun close() {
        statement.close()
    }
}