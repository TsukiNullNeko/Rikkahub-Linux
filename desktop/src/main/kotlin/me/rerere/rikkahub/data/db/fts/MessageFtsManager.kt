package me.rerere.rikkahub.data.db.fts

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.rikkahub.data.db.AppDatabase
import me.rerere.rikkahub.data.db.SQLiteConfiguration
import me.rerere.rikkahub.data.model.Conversation
import java.sql.Connection
import java.sql.DriverManager

private const val TAG = "MessageFtsManager"

data class MessageSearchResult(
    val nodeId: String,
    val messageId: String,
    val conversationId: String,
    val title: String,
    val updateAt: java.time.Instant,
    val snippet: String,
)

enum class MessageSearchSort(val orderBy: String) {
    RELEVANCE("rank, update_at DESC"),
    NEWEST_FIRST("update_at DESC, rank"),
    OLDEST_FIRST("update_at ASC, rank"),
}

/**
 * Full-text search over message contents, backed by SQLite FTS5 through JDBC.
 */
class MessageFtsManager(private val database: AppDatabase) {

    suspend fun indexConversation(conversation: Conversation) = withContext(Dispatchers.IO) {
        withConnection { connection ->
            connection.prepareStatement("DELETE FROM message_fts WHERE conversation_id = ?").use { statement ->
                statement.setString(1, conversation.id)
                statement.executeUpdate()
            }
            connection.prepareStatement(
                "INSERT INTO message_fts (text, node_id, message_id, conversation_id, title, update_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)"
            ).use { statement ->
                conversation.messageNodes.forEach { node ->
                    val text = node.messages.joinToString("\n") { message ->
                        message.parts.filterIsInstance<me.rerere.ai.ui.UIMessagePart.Text>()
                            .joinToString("\n") { it.text }
                    }
                    if (text.isBlank()) return@forEach
                    statement.setString(1, text)
                    statement.setString(2, node.id.toString())
                    statement.setString(3, node.currentMessage?.id?.toString() ?: node.id.toString())
                    statement.setString(4, conversation.id)
                    statement.setString(5, conversation.title)
                    statement.setLong(6, conversation.updateAt.toEpochMilli())
                    statement.executeUpdate()
                }
            }
        }
    }

    suspend fun deleteConversation(conversationId: String) = withContext(Dispatchers.IO) {
        withConnection { connection ->
            connection.prepareStatement("DELETE FROM message_fts WHERE conversation_id = ?").use { statement ->
                statement.setString(1, conversationId)
                statement.executeUpdate()
            }
        }
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        withConnection { connection ->
            connection.createStatement().use { it.executeUpdate("DELETE FROM message_fts") }
        }
    }

    suspend fun search(
        keyword: String,
        sort: MessageSearchSort = MessageSearchSort.RELEVANCE,
        assistantIds: Collection<String>? = null,
    ): List<MessageSearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<MessageSearchResult>()
        if (assistantIds != null && assistantIds.isEmpty()) return@withContext results
        if (keyword.isBlank()) return@withContext results

        val assistantFilter = if (assistantIds != null) {
            "AND EXISTS (SELECT 1 FROM conversationentity AS conversation " +
                "WHERE conversation.id = message_fts.conversation_id " +
                "AND conversation.assistant_id IN (" + assistantIds.joinToString(",") { "?" } + "))"
        } else {
            ""
        }

        val sql = "SELECT conversation_id, message_id, node_id, title, " +
            "snippet(message_fts, 0, '', '', '…', 16) AS snippet, update_at " +
            "FROM message_fts WHERE message_fts MATCH ? $assistantFilter " +
            "ORDER BY " + sort.orderBy + " LIMIT 200"

        withConnection { connection ->
            connection.prepareStatement(sql).use { statement ->
                var index = 1
                statement.setString(index++, toFtsQuery(keyword))
                assistantIds?.forEach { statement.setString(index++, it) }
                statement.executeQuery().use { rs ->
                    while (rs.next()) {
                        results.add(
                            MessageSearchResult(
                                nodeId = rs.getString("node_id") ?: "",
                                messageId = rs.getString("message_id") ?: "",
                                conversationId = rs.getString("conversation_id") ?: "",
                                title = rs.getString("title") ?: "",
                                updateAt = java.time.Instant.ofEpochMilli(rs.getLong("update_at")),
                                snippet = rs.getString("snippet") ?: "",
                            )
                        )
                    }
                }
            }
        }
        results
    }

    /** Converts user input into a safe FTS5 prefix query. */
    private fun toFtsQuery(keyword: String): String {
        val terms = keyword.split(Regex("\\s+"))
            .map { it.replace("\"", "").trim() }
            .filter { it.isNotEmpty() }
        if (terms.isEmpty()) return "\"\""
        return terms.joinToString(" AND ") { "\"" + it + "\"*" }
    }

    private fun <T> withConnection(block: (Connection) -> T): T? {
        return try {
            DriverManager.getConnection("jdbc:sqlite:" + SQLiteConfiguration.databasePath()).use { connection ->
                block(connection)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fts operation failed", e)
            null
        }
    }
}