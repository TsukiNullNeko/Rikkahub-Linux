package me.rerere.rikkahub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.event.AppEvent
import me.rerere.rikkahub.data.event.AppEventBus
import org.koin.compose.koinInject
import kotlin.uuid.Uuid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.rerere.rikkahub.data.db.DatabaseMigrationTracker
import me.rerere.rikkahub.data.db.MigrationState
import me.rerere.rikkahub.data.repository.MediaCreationRepository
import me.rerere.rikkahub.ui.components.ui.TTSController
import me.rerere.rikkahub.ui.context.LocalASRState
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.context.LocalSettings
import me.rerere.rikkahub.ui.context.LocalSharedTransitionScope
import me.rerere.rikkahub.ui.context.LocalTTSState
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.context.Navigator
import me.rerere.rikkahub.ui.hooks.readBooleanPreference
import me.rerere.rikkahub.ui.hooks.readStringPreference
import me.rerere.rikkahub.ui.hooks.rememberCustomAsrState
import me.rerere.rikkahub.ui.hooks.rememberCustomTtsState
import me.rerere.rikkahub.ui.pages.assistant.AssistantPage
import me.rerere.rikkahub.ui.pages.assistant.detail.AssistantBasicPage
import me.rerere.rikkahub.ui.pages.assistant.detail.AssistantDetailPage
import me.rerere.rikkahub.ui.pages.assistant.detail.AssistantExtensionsPage
import me.rerere.rikkahub.ui.pages.assistant.detail.AssistantLocalToolPage
import me.rerere.rikkahub.ui.pages.assistant.detail.AssistantMcpPage
import me.rerere.rikkahub.ui.pages.assistant.detail.AssistantMemoryPage
import me.rerere.rikkahub.ui.pages.assistant.detail.AssistantPromptPage
import me.rerere.rikkahub.ui.pages.assistant.detail.AssistantRequestPage
import me.rerere.rikkahub.ui.pages.backup.BackupPage
import me.rerere.rikkahub.ui.pages.chat.ChatPage
import me.rerere.rikkahub.ui.pages.debug.DebugPage
import me.rerere.rikkahub.ui.pages.extensions.ExtensionsPage
import me.rerere.rikkahub.ui.pages.extensions.PromptPage
import me.rerere.rikkahub.ui.pages.extensions.QuickMessagesPage
import me.rerere.rikkahub.ui.pages.extensions.skills.SkillDetailPage
import me.rerere.rikkahub.ui.pages.extensions.skills.SkillsPage
import me.rerere.rikkahub.ui.pages.extensions.workspace.WorkspaceDetailPage
import me.rerere.rikkahub.ui.pages.extensions.workspace.WorkspaceFileEditorPage
import me.rerere.rikkahub.ui.pages.extensions.workspace.WorkspacePage
import me.rerere.rikkahub.ui.pages.extensions.workspace.WorkspaceTerminalPage
import me.rerere.rikkahub.ui.pages.favorite.FavoritePage
import me.rerere.rikkahub.ui.pages.history.HistoryPage
import me.rerere.rikkahub.ui.pages.imggen.ImageGenPage
import me.rerere.rikkahub.ui.pages.log.LogPage
import me.rerere.rikkahub.ui.pages.mediacreation.MediaCreationPage
import me.rerere.rikkahub.ui.pages.mediacreation.MediaCreationSessionsPage
import me.rerere.rikkahub.ui.pages.memory.MemoryFilePage
import me.rerere.rikkahub.ui.pages.search.SearchPage
import me.rerere.rikkahub.ui.pages.setting.SettingAboutPage
import me.rerere.rikkahub.ui.pages.setting.SettingDonatePage
import me.rerere.rikkahub.ui.pages.setting.SettingFilesPage
import me.rerere.rikkahub.ui.pages.setting.SettingMcpPage
import me.rerere.rikkahub.ui.pages.setting.SettingMediaPage
import me.rerere.rikkahub.ui.pages.setting.SettingModelPage
import me.rerere.rikkahub.ui.pages.setting.SettingPage
import me.rerere.rikkahub.ui.pages.setting.SettingPreferencesGeneralPage
import me.rerere.rikkahub.ui.pages.setting.SettingPreferencesNetworkPage
import me.rerere.rikkahub.ui.pages.setting.SettingPreferencesNotificationPage
import me.rerere.rikkahub.ui.pages.setting.SettingPreferencesPage
import me.rerere.rikkahub.ui.pages.setting.SettingPreferencesThemePage
import me.rerere.rikkahub.ui.pages.setting.SettingPreferencesUIPage
import me.rerere.rikkahub.ui.pages.setting.SettingProviderDetailPage
import me.rerere.rikkahub.ui.pages.setting.SettingProviderPage
import me.rerere.rikkahub.ui.pages.setting.SettingSearchDetailPage
import me.rerere.rikkahub.ui.pages.setting.SettingSearchPage
import me.rerere.rikkahub.ui.pages.setting.SettingSpeechPage
import me.rerere.rikkahub.ui.pages.setting.SettingThemePage
import me.rerere.rikkahub.ui.pages.setting.SettingWebPage
import me.rerere.rikkahub.ui.pages.share.handler.ShareHandlerPage
import me.rerere.rikkahub.ui.pages.stats.StatsPage
import me.rerere.rikkahub.ui.pages.translator.TranslatorPage
import me.rerere.rikkahub.ui.pages.webview.WebViewPage
import me.rerere.rikkahub.ui.theme.LocalDarkMode
import me.rerere.rikkahub.ui.theme.RikkahubTheme
import me.rerere.rikkahub.utils.CrashHandler
import me.rerere.rikkahub.utils.openUsageAccessSettings
import me.rerere.workspace.WorkspaceStorageArea
import okhttp3.OkHttpClient

@Composable
fun AppRoutes() {
    val toastState = rememberToasterState()
    val settingsStore = koinInject<SettingsStore>()
    val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle()
    val tts = rememberCustomTtsState()
    val asr = rememberCustomAsrState()
    val eventBus = koinInject<AppEventBus>()
    LaunchedEffect(tts) {
        eventBus.events.collect { event ->
            when (event) {
                is AppEvent.Speak -> tts.speak(event.text)
                is AppEvent.OpenUsageAccessSettings -> Unit // desktop: no usage-access settings screen
                is AppEvent.ChatGenerationUpdate -> Unit // 由 ChatNotificationManager 消费
                is AppEvent.ChatGenerationEnded -> Unit // 由 ChatNotificationManager 消费
            }
        }
    }
    val migrationState by DatabaseMigrationTracker.state.collectAsStateWithLifecycle()

    // Desktop always starts on a fresh conversation.
    val startScreen = Screen.Chat(id = Uuid.random().toString())
    val backStack = rememberNavBackStack(startScreen)

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalNavController provides Navigator(backStack),
            LocalSharedTransitionScope provides this,
            LocalSettings provides settings,
            LocalToaster provides toastState,
            LocalTTSState provides tts,
            LocalASRState provides asr,
        ) {
            Toaster(
                state = toastState,
                darkTheme = LocalDarkMode.current,
                richColors = true,
                alignment = Alignment.TopCenter,
                showCloseButton = true,
            )
            TTSController()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    modifier = Modifier.fillMaxSize(),
                    onBack = { backStack.removeLastOrNull() },
                    transitionSpec = {
                        if (backStack.size == 1) fadeIn() togetherWith fadeOut()
                        else {
                            slideInHorizontally { it } togetherWith
                                slideOutHorizontally { -it / 2 } + scaleOut(targetScale = 0.7f) + fadeOut()
                        }
                    },
                    popTransitionSpec = {
                        slideInHorizontally { -it / 2 } + scaleIn(initialScale = 0.7f) + fadeIn() togetherWith
                            slideOutHorizontally { it }
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally { -it / 2 } + scaleIn(initialScale = 0.7f) + fadeIn() togetherWith
                            slideOutHorizontally { it }
                    },
                    entryProvider = entryProvider {
                        entry<Screen.Chat>(
                            metadata = NavDisplay.transitionSpec { fadeIn() togetherWith fadeOut() }
                                + NavDisplay.popTransitionSpec { fadeIn() togetherWith fadeOut() }
                        ) { key ->
                            ChatPage(
                                id = Uuid.parse(key.id),
                                text = key.text,
                                files = key.files.map { it.toUri() },
                                nodeId = key.nodeId?.let { Uuid.parse(it) }
                            )
                        }

                        entry<Screen.ShareHandler> { key ->
                            ShareHandlerPage(
                                text = key.text,
                                image = key.streamUri
                            )
                        }

                        entry<Screen.History> {
                            HistoryPage()
                        }

                        entry<Screen.Favorite> {
                            FavoritePage()
                        }

                        entry<Screen.Assistant> {
                            AssistantPage()
                        }

                        entry<Screen.AssistantDetail> { key ->
                            AssistantDetailPage(key.id)
                        }

                        entry<Screen.AssistantBasic> { key ->
                            AssistantBasicPage(key.id)
                        }

                        entry<Screen.AssistantPrompt> { key ->
                            AssistantPromptPage(key.id)
                        }

                        entry<Screen.AssistantMemory> { key ->
                            AssistantMemoryPage(key.id)
                        }

                        entry<Screen.MemoryFile> { key ->
                            MemoryFilePage(memoryId = key.memoryId, path = key.path)
                        }

                        entry<Screen.AssistantRequest> { key ->
                            AssistantRequestPage(key.id)
                        }

                        entry<Screen.AssistantMcp> { key ->
                            AssistantMcpPage(key.id)
                        }

                        entry<Screen.AssistantLocalTool> { key ->
                            AssistantLocalToolPage(key.id)
                        }

                        entry<Screen.AssistantInjections> { key ->
                            AssistantExtensionsPage(key.id)
                        }

                        entry<Screen.Translator> {
                            TranslatorPage()
                        }

                        entry<Screen.Setting> {
                            SettingPage()
                        }

                        entry<Screen.Backup> {
                            BackupPage()
                        }

                        entry<Screen.ImageGen> {
                            ImageGenPage()
                        }

                        entry<Screen.MediaCreationSessions> {
                            MediaCreationSessionsPage()
                        }

                        entry<Screen.MediaCreation> { key ->
                            MediaCreationPage(key.id)
                        }

                        entry<Screen.WebView> { key ->
                            WebViewPage(key.url, key.contentId)
                        }

                        entry<Screen.SettingTheme> {
                            SettingThemePage()
                        }

                        entry<Screen.SettingPreferences> {
                            SettingPreferencesPage()
                        }

                        entry<Screen.SettingPreferencesTheme> {
                            SettingPreferencesThemePage()
                        }

                        entry<Screen.SettingPreferencesNotification> {
                            SettingPreferencesNotificationPage()
                        }

                        entry<Screen.SettingPreferencesGeneral> {
                            SettingPreferencesGeneralPage()
                        }

                        entry<Screen.SettingPreferencesUI> {
                            SettingPreferencesUIPage()
                        }

                        entry<Screen.SettingPreferencesNetwork> {
                            SettingPreferencesNetworkPage()
                        }

                        entry<Screen.SettingProvider> {
                            SettingProviderPage()
                        }

                        entry<Screen.SettingProviderDetail> { key ->
                            val id = Uuid.parse(key.providerId)
                            SettingProviderDetailPage(id = id)
                        }

                        entry<Screen.SettingModels> {
                            SettingModelPage()
                        }

                        entry<Screen.SettingAbout> {
                            SettingAboutPage()
                        }

                        entry<Screen.SettingSearch> {
                            SettingSearchPage()
                        }

                        entry<Screen.SettingSearchDetail> { key ->
                            val id = Uuid.parse(key.serviceId)
                            SettingSearchDetailPage(id)
                        }

                        entry<Screen.SettingSpeech> {
                            SettingSpeechPage()
                        }

                        entry<Screen.SettingMedia> {
                            SettingMediaPage()
                        }

                        entry<Screen.SettingMcp> {
                            SettingMcpPage()
                        }

                        entry<Screen.SettingDonate> {
                            SettingDonatePage()
                        }

                        entry<Screen.SettingFiles> {
                            SettingFilesPage()
                        }

                        entry<Screen.SettingWeb> {
                            SettingWebPage()
                        }

                        entry<Screen.Debug> {
                            DebugPage()
                        }

                        entry<Screen.Log> {
                            LogPage()
                        }

                        entry<Screen.Extensions> {
                            ExtensionsPage()
                        }

                        entry<Screen.QuickMessages> {
                            QuickMessagesPage()
                        }

                        entry<Screen.Prompts> {
                            PromptPage()
                        }

                        entry<Screen.Skills> {
                            SkillsPage()
                        }

                        entry<Screen.Workspaces> {
                            WorkspacePage()
                        }

                        entry<Screen.WorkspaceDetail> { key ->
                            WorkspaceDetailPage(key.id)
                        }

                        entry<Screen.WorkspaceTerminal> { key ->
                            WorkspaceTerminalPage(key.id)
                        }

                        entry<Screen.WorkspaceFileEditor> { key ->
                            WorkspaceFileEditorPage(
                                id = key.id,
                                area = WorkspaceStorageArea.valueOf(key.area),
                                path = key.path,
                            )
                        }

                        entry<Screen.SkillDetail> { key ->
                            SkillDetailPage(skillName = key.skillName)
                        }

                        entry<Screen.MessageSearch> {
                            SearchPage()
                        }

                        entry<Screen.Stats> {
                            StatsPage()
                        }
                    }
                )
                if (BuildConfig.DEBUG) {
                    Text(
                        text = "[开发模式]",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
                AnimatedVisibility(
                    visible = migrationState is MigrationState.Migrating,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val state = migrationState as? MigrationState.Migrating
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = stringResource(R.string.db_migrating),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (state != null) {
                                Text(
                                    text = "v${state.from} → v${state.to}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}