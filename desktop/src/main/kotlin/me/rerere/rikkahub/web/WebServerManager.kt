package me.rerere.rikkahub.web

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.rerere.rikkahub.AppScope
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.files.FilesManager
import me.rerere.rikkahub.data.repository.ConversationRepository
import me.rerere.rikkahub.data.repository.FolderRepository
import me.rerere.rikkahub.service.ChatService

/**
 * Linux 桌面版不内置 Web 服务（原 Android 版的 Ktor 网页端已按需求移除）。
 * 保留类型与状态 API 以便设置页等相关代码正常编译。
 */
data class WebServerState(
    val isRunning: Boolean = false,
    val isLoading: Boolean = false,
    val port: Int = 8080,
    val serviceName: String = DEFAULT_SERVICE_NAME,
    val localhostOnly: Boolean = false,
    val hostname: String? = null,
    val address: String? = null,
    val error: String? = null
)

const val DEFAULT_SERVICE_NAME = "RikkaHub"

class WebServerManager(
    private val context: Context,
    private val appScope: AppScope,
    private val chatService: ChatService,
    private val conversationRepo: ConversationRepository,
    private val folderRepo: FolderRepository,
    private val settingsStore: SettingsStore,
    private val filesManager: FilesManager
) {
    private val _state = MutableStateFlow(WebServerState())
    val state: StateFlow<WebServerState> = _state.asStateFlow()

    fun start(
        port: Int = 8080,
        serviceName: String = DEFAULT_SERVICE_NAME,
        localhostOnly: Boolean = false
    ) {
        Log.i("WebServerManager", "web server is disabled on Linux desktop")
        _state.value = WebServerState(error = "Web server is not available on Linux desktop")
    }

    fun reportError(message: String) {
        _state.value = WebServerState(error = message)
    }

    fun stop() {
        _state.value = WebServerState()
    }

    fun restart(
        port: Int = 8080,
        serviceName: String = DEFAULT_SERVICE_NAME,
        localhostOnly: Boolean = false
    ) {
        stop()
    }
}

/** Errors previously raised by the web module's API layer. */
class NotFoundException(message: String) : RuntimeException(message)
class BadRequestException(message: String) : RuntimeException(message)
