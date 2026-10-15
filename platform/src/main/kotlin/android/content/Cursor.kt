package android.content

// Minimal Cursor placeholder (used by a few call sites that query MediaStore / contacts on Android).
interface Cursor : AutoCloseable {
    val count: Int
    val columnNames: Array<String>
    fun moveToFirst(): Boolean
    fun moveToNext(): Boolean
    fun moveToPosition(position: Int): Boolean
    fun getColumnIndex(name: String): Int
    fun getColumnIndexOrThrow(name: String): Int
    fun getString(index: Int): String
    fun getInt(index: Int): Int
    fun getLong(index: Int): Long
    fun getBlob(index: Int): ByteArray?
    fun isClosed(): Boolean
    fun isAfterLast(): Boolean
    fun isBeforeFirst(): Boolean
    fun isFirst(): Boolean
    fun isLast(): Boolean
    fun getPosition(): Int
    override fun close()
}

class MatrixCursor(columnNames: Array<String>, initialCapacity: Int = 16) : Cursor {
    private val rows = mutableListOf<Array<Any?>>()
    private var pos = -1
    override val columnNames: Array<String> = columnNames
    override val count: Int get() = rows.size
    fun addRow(values: Array<Any?>) { rows.add(values) }
    override fun moveToFirst(): Boolean { pos = if (rows.isEmpty()) -1 else 0; return pos == 0 }
    override fun moveToNext(): Boolean { if (pos < rows.size - 1) { pos++; return true }; return false }
    override fun moveToPosition(position: Int): Boolean { if (position < rows.size) { pos = position; return true }; return false }
    override fun getColumnIndex(name: String): Int = columnNames.indexOf(name)
    override fun getColumnIndexOrThrow(name: String): Int = getColumnIndex(name).also { if (it < 0) throw IllegalArgumentException("no column " + name) }
    override fun getString(index: Int): String = (rows.getOrNull(pos)?.getOrNull(index) as? String) ?: ""
    override fun getInt(index: Int): Int = (rows.getOrNull(pos)?.getOrNull(index) as? Number)?.toInt() ?: 0
    override fun getLong(index: Int): Long = (rows.getOrNull(pos)?.getOrNull(index) as? Number)?.toLong() ?: 0L
    override fun getBlob(index: Int): ByteArray? = rows.getOrNull(pos)?.getOrNull(index) as? ByteArray
    override fun isClosed(): Boolean = false
    override fun isAfterLast(): Boolean = pos >= rows.size
    override fun isBeforeFirst(): Boolean = pos < 0
    override fun isFirst(): Boolean = pos == 0
    override fun isLast(): Boolean = pos == rows.size - 1
    override fun getPosition(): Int = pos
    override fun close() {}
}

class CursorWindow(name: String = "cursor") {
    private val rows = mutableListOf<Array<Any?>>()
    fun getNumRows(): Int = rows.size
    fun getStartPosition(): Int = 0
    fun setStartPosition(position: Int) {}
    fun setNumColumns(columnNum: Int): Boolean = true
    fun clear() { rows.clear() }
    fun close() {}
    fun addRow(values: Array<Any?>): Boolean { rows.add(values); return true }
    fun getString(row: Int, column: Int): String? = rows.getOrNull(row)?.getOrNull(column) as? String
    fun getLong(row: Int, column: Int): Long = (rows.getOrNull(row)?.getOrNull(column) as? Number)?.toLong() ?: 0L
    fun getInt(row: Int, column: Int): Int = (rows.getOrNull(row)?.getOrNull(column) as? Number)?.toInt() ?: 0
}