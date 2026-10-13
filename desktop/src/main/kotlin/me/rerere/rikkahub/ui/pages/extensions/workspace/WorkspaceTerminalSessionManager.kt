package me.rerere.rikkahub.ui.pages.extensions.workspace

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rerere.rikkahub.AppScope
import java.util.concurrent.atomic.AtomicLong

private const val TAG = "WorkspaceTerminal"

/**
 * Owns workspace terminal sessions independently from the terminal page lifecycle:
 * navigating away keeps every shell alive until its tab is closed.
 */
class WorkspaceTerminalSessionManager internal constructor(
    context: Context,
    private val appScope: AppScope,
) {
    private val appContext = context.applicationContext
    private val workspaceStates = MutableStateFlow<Map<String, WorkspaceTerminalTabsState>>(emptyMap())
    private val nextTabId = AtomicLong(1)
    private val creationJobs = mutableMapOf<String, Job>()

    internal fun observeWorkspace(root: String): Flow<WorkspaceTerminalTabsState> =
        workspaceStates
            .map { states -> states[root] ?: WorkspaceTerminalTabsState() }
            .distinctUntilChanged()

    internal fun ensureSession(root: String, shellCompatibilityMode: Boolean) {
        launchCreateTab(root = root, onlyIfEmpty = true, shellCompatibilityMode = shellCompatibilityMode)
    }

    internal fun createTab(root: String, shellCompatibilityMode: Boolean) {
        launchCreateTab(root = root, onlyIfEmpty = false, shellCompatibilityMode = shellCompatibilityMode)
    }

    internal fun selectTab(root: String, tabId: Long) {
        updateState(root) { state ->
            if (state.tabs.none { it.id == tabId }) state else state.copy(selectedTabId = tabId)
        }
    }

    internal fun closeTab(root: String, tabId: Long) {
        var closedTab: WorkspaceTerminalTab? = null
        updateState(root) { state ->
            val closedIndex = state.tabs.indexOfFirst { it.id == tabId }
            if (closedIndex < 0) return@updateState state

            closedTab = state.tabs[closedIndex]
            val remainingTabs = state.tabs.filterNot { it.id == tabId }
            val selectedTabId = if (state.selectedTabId == tabId) {
                remainingTabs.getOrNull(closedIndex)?.id
                    ?: remainingTabs.getOrNull(closedIndex - 1)?.id
            } else {
                state.selectedTabId
            }
            state.copy(
                tabs = remainingTabs,
                selectedTabId = selectedTabId,
            )
        }
        closedTab?.session?.destroy()
    }

    /** Stops all sessions owned by [root] before its files are replaced or the workspace is deleted. */
    internal suspend fun closeWorkspace(root: String) = withContext(Dispatchers.Main.immediate) {
        creationJobs[root]?.cancelAndJoin()

        val state = workspaceStates.getAndUpdate { states -> states - root }[root]
            ?: return@withContext
        state.tabs.forEach { tab ->
            tab.session.destroy()
        }
    }

    private fun launchCreateTab(root: String, onlyIfEmpty: Boolean, shellCompatibilityMode: Boolean) {
        if (root in creationJobs) return

        lateinit var job: Job
        job = appScope.launch(start = CoroutineStart.LAZY) {
            try {
                createTab(root = root, onlyIfEmpty = onlyIfEmpty, shellCompatibilityMode = shellCompatibilityMode)
            } finally {
                creationJobs.remove(root, job)
            }
        }
        creationJobs[root] = job
        job.start()
    }

    private suspend fun createTab(root: String, onlyIfEmpty: Boolean, shellCompatibilityMode: Boolean) =
        withContext(Dispatchers.Main.immediate) {
            val initialState = currentState(root)
            if (initialState.isCreating || (onlyIfEmpty && initialState.tabs.isNotEmpty())) {
                return@withContext
            }
            updateState(root) { it.copy(isCreating = true) }

            val prepared = if (initialState.readiness == WorkspaceTerminalReadiness.Ready) {
                true
            } else {
                try {
                    withContext(Dispatchers.IO) {
                        if (!workspaceRootfsReady(appContext, root)) {
                            false
                        } else {
                            prepareWorkspaceTerminalSession(appContext, root)
                            true
                        }
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    Log.e(TAG, "Failed to prepare terminal for workspace $root", error)
                    false
                }
            }

            if (!prepared) {
                updateState(root) {
                    it.copy(
                        readiness = WorkspaceTerminalReadiness.NotInstalled,
                        isCreating = false,
                    )
                }
                return@withContext
            }

            val tabId = nextTabId.getAndIncrement()
            val tabNumber = currentState(root).nextTabNumber
            val session = runCatching {
                WorkspaceTerminalSession(
                    id = tabId,
                    workingDir = workspaceTerminalWorkingDir(appContext, root),
                    onOutput = { },
                    onTitleUpdated = { _, title -> updateTitle(root, tabId, title) },
                    onFinished = { markFinished(root = root, tabId = tabId) },
                )
            }.onFailure { error ->
                Log.e(TAG, "Failed to create terminal for workspace $root", error)
            }.getOrNull()

            if (session == null) {
                updateState(root) { it.copy(isCreating = false) }
                return@withContext
            }

            val tab = WorkspaceTerminalTab(
                id = tabId,
                number = tabNumber,
                session = session,
            )
            updateState(root) { state ->
                state.copy(
                    tabs = state.tabs + tab,
                    selectedTabId = tab.id,
                    readiness = WorkspaceTerminalReadiness.Ready,
                    isCreating = false,
                    nextTabNumber = tabNumber + 1,
                )
            }
        }

    private fun updateTitle(root: String, tabId: Long, title: String?) {
        val normalizedTitle = title?.trim()?.takeIf { it.isNotEmpty() }
        workspaceStates.update { states ->
            val state = states[root] ?: return@update states
            if (state.tabs.none { it.id == tabId }) return@update states

            states + (root to state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) tab.copy(title = normalizedTitle) else tab
                },
            ))
        }
    }

    private fun markFinished(root: String, tabId: Long) {
        workspaceStates.update { states ->
            val state = states[root] ?: return@update states
            if (state.tabs.none { it.id == tabId }) return@update states
            states + (root to state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) tab.copy(finished = true) else tab
                },
            ))
        }
    }

    private fun currentState(root: String): WorkspaceTerminalTabsState =
        workspaceStates.value[root] ?: WorkspaceTerminalTabsState()

    private fun updateState(
        root: String,
        transform: (WorkspaceTerminalTabsState) -> WorkspaceTerminalTabsState,
    ) {
        workspaceStates.update { states ->
            val current = states[root] ?: WorkspaceTerminalTabsState()
            states + (root to transform(current))
        }
    }
}

internal data class WorkspaceTerminalTabsState(
    val tabs: List<WorkspaceTerminalTab> = emptyList(),
    val selectedTabId: Long? = null,
    val readiness: WorkspaceTerminalReadiness = WorkspaceTerminalReadiness.Loading,
    val isCreating: Boolean = false,
    val nextTabNumber: Int = 1,
)

internal data class WorkspaceTerminalTab(
    val id: Long,
    val number: Int,
    val session: WorkspaceTerminalSession,
    val title: String? = null,
    val finished: Boolean = false,
)

internal enum class WorkspaceTerminalReadiness {
    Loading,
    Ready,
    NotInstalled,
}
