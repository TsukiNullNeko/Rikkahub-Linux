package me.rerere.rikkahub.ui.pages.extensions.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Cancel01
import me.rerere.hugeicons.stroke.PlusSign
import me.rerere.hugeicons.stroke.Terminal
import me.rerere.rikkahub.ui.pages.extensions.workspace.WorkspaceTerminalReadiness.NotInstalled
import me.rerere.rikkahub.ui.pages.extensions.workspace.WorkspaceTerminalReadiness.Ready
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Desktop workspace terminal: a local shell running inside the workspace directory.
 */
@Composable
fun WorkspaceTerminalPage(id: String) {
    val vm: WorkspaceDetailVM = koinViewModel(parameters = { org.koin.core.parameter.parametersOf(id) })
    val state by vm.state.collectAsStateWithLifecycle()
    val sessionManager: WorkspaceTerminalSessionManager = koinInject()
    val root = state.workspace?.root
    val tabsState by remember(root) {
        root?.let { sessionManager.observeWorkspace(it) }
            ?: kotlinx.coroutines.flow.flowOf(WorkspaceTerminalTabsState())
    }.collectAsStateWithLifecycle(initialValue = WorkspaceTerminalTabsState())

    LaunchedEffect(root) {
        root?.let { sessionManager.ensureSession(it, false) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Column(modifier = Modifier.fillMaxSize()) {
            TerminalTabBar(
                state = tabsState,
                onCreateTab = { root?.let { sessionManager.createTab(it, false) } },
                onSelectTab = { tabId -> root?.let { sessionManager.selectTab(it, tabId) } },
                onCloseTab = { tabId -> root?.let { sessionManager.closeTab(it, tabId) } },
            )

            when {
                root == null -> CenteredMessage("Workspace is not available")
                tabsState.readiness == NotInstalled -> CenteredMessage("Workspace shell is not ready")
                tabsState.tabs.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                else -> {
                    val selected = tabsState.tabs.firstOrNull { it.id == tabsState.selectedTabId }
                        ?: tabsState.tabs.first()
                    TerminalPane(session = selected.session)
                }
            }
        }
    }
}

@Composable
private fun TerminalTabBar(
    state: WorkspaceTerminalTabsState,
    onCreateTab: () -> Unit,
    onSelectTab: (Long) -> Unit,
    onCloseTab: (Long) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.tabs.forEach { tab ->
                val selected = tab.id == state.selectedTabId
                Surface(
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    ) {
                        Text(
                            text = tab.title ?: ("Shell " + tab.number),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        IconButton(onClick = { onCloseTab(tab.id) }, modifier = Modifier.width(28.dp)) {
                            Icon(
                                HugeIcons.Cancel01,
                                contentDescription = "Close tab",
                                modifier = Modifier.padding(2.dp),
                            )
                        }
                    }
                }
                if (!selected) {
                    TextButton(onClick = { onSelectTab(tab.id) }) { Text("") }
                }
            }
            IconButton(onClick = onCreateTab, enabled = !state.isCreating) {
                Icon(HugeIcons.PlusSign, contentDescription = "New terminal")
            }
        }
    }
}

@Composable
private fun TerminalPane(session: WorkspaceTerminalSession) {
    val lines = remember(session) { mutableStateListOf<String>() }
    var input by remember(session) { mutableStateOf("") }
    val listState = rememberLazyListState()

    DisposableEffect(session) {
        // Seed with existing scrollback, then stream new output.
        lines.clear()
        session.snapshot().split("\n").forEach { lines.add(it) }
        val listener: (String) -> Unit = { chunk ->
            chunk.split("\n").forEach { lines.add(it) }
        }
        session.addOutputListener(listener)
        onDispose { session.removeOutputListener(listener) }
    }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF14161A))
                .padding(8.dp),
        ) {
            items(lines) { line ->
                Text(
                    text = line.ifEmpty { " " },
                    color = Color(0xFFD7DAE0),
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$ ",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (input.isEmpty()) {
                        Text(
                            "Type a command and press Enter",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                },
            )
            TextButton(
                onClick = {
                    val command = input
                    input = ""
                    session.sendCommand(command)
                }
            ) {
                Text("Run")
            }
        }
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}
