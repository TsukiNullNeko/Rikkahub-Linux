package me.rerere.ui.webview

import android.content.Intent
import android.webkit.ConsoleMessage
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Desktop WebView replacement.
 *
 * The Linux build has no embedded browser engine, so HTML content is rendered by the
 * system browser. The state API mirrors the Android implementation so the rest of the
 * app keeps working unchanged.
 */
@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    onCreated: (WebView) -> Unit = {},
    onUpdated: (WebView) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webView = remember { WebView(context).also { it.settings = state.settings.toSettings() } }

    LaunchedEffect(state.content, state.forceReload) {
        webView.webChromeClient = null
        state.webView = webView
        state.isLoading = true
        when (val content = state.content) {
            is WebContent.Url -> {
                webView.loadUrl(content.url, content.additionalHttpHeaders)
                state.currentUrl = content.url
                state.pageTitle = content.url
            }
            is WebContent.Data -> {
                state.currentUrl = content.baseUrl ?: WEB_VIEW_BASE_URL
                state.pageTitle = "HTML"
            }
            WebContent.NavigatorOnly -> {}
        }
        state.canGoBack = webView.canGoBack()
        state.canGoForward = webView.canGoForward()
        state.isLoading = false
        state.loadingProgress = 0f
        state.forceReload = false
        onCreated(webView)
        onUpdated(webView)
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Inline web content is not available on Linux.",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = state.currentUrl ?: "",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = {
                    val url = state.currentUrl
                    val data = (state.content as? WebContent.Data)?.data
                    when {
                        data != null -> scope.launch { openHtmlInBrowser(data) }
                        url != null -> context.openUri(android.net.Uri.parse(url))
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Open in browser")
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(
                progress = { state.loadingProgress },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private suspend fun openHtmlInBrowser(html: String) = withContext(Dispatchers.IO) {
    runCatching {
        val dir = android.content.AppContext.cacheDir.resolve("webview")
        dir.mkdirs()
        val file = File(dir, "preview_" + System.currentTimeMillis() + ".html")
        file.writeText(html)
        android.content.AppContext.openExternally(android.net.Uri.fromFile(file))
    }
}

// --- State and Content Definition ---
sealed class WebContent {
    data class Url(
        val url: String,
        val additionalHttpHeaders: Map<String, String> = emptyMap(),
        val clearHistory: Boolean = false
    ) : WebContent()

    data class Data(
        val data: String,
        val baseUrl: String? = null,
        val encoding: String = "utf-8",
        val mimeType: String? = null,
        val historyUrl: String? = null
    ) : WebContent()

    data object NavigatorOnly : WebContent()
}

@Stable
class WebViewState(
    initialContent: WebContent = WebContent.NavigatorOnly,
    val interfaces: Map<String, Any> = emptyMap(),
    val settings: WebSettings.() -> Unit = {}
) {
    var content: WebContent by mutableStateOf(initialContent)
    internal var forceReload: Boolean by mutableStateOf(false)
    internal var lastLoadedData: WebContent.Data? = null

    var isLoading: Boolean by mutableStateOf(false)
    var loadingProgress: Float by mutableFloatStateOf(0f)

    var pageTitle: String? by mutableStateOf(null)
    var currentUrl: String? by mutableStateOf(null)

    var canGoBack: Boolean by mutableStateOf(false)
    var canGoForward: Boolean by mutableStateOf(false)

    var consoleMessages: List<ConsoleMessage> by mutableStateOf(emptyList())

    var javaScriptEnabled: Boolean by mutableStateOf(true)

    var webView: WebView? by mutableStateOf(null)

    fun loadUrl(url: String, additionalHttpHeaders: Map<String, String> = emptyMap()) {
        forceReload = (content is WebContent.Url && (content as WebContent.Url).url == url) || forceReload
        content = WebContent.Url(url, additionalHttpHeaders)
    }

    fun loadData(
        data: String,
        baseUrl: String? = null,
        encoding: String = "utf-8",
        mimeType: String? = null,
        historyUrl: String? = null
    ) {
        content = WebContent.Data(data, baseUrl, encoding, mimeType, historyUrl)
    }

    fun goBack() {
        if (webView?.goBack() == true) {
            canGoBack = webView?.canGoBack() == true
            canGoForward = webView?.canGoForward() == true
            currentUrl = webView?.url
        }
    }

    fun goForward() {
        if (webView?.goForward() == true) {
            canGoBack = webView?.canGoBack() == true
            canGoForward = webView?.canGoForward() == true
            currentUrl = webView?.url
        }
    }

    fun reload() {
        forceReload = true
        webView?.reload()
        if (content is WebContent.Data) {
            content = (content as WebContent.Data).copy()
        }
    }

    fun stopLoading() {
        webView?.stopLoading()
    }

    fun clearHistory() {
        webView?.clearHistory()
    }

    fun pushConsoleMessage(message: ConsoleMessage) {
        consoleMessages = consoleMessages + message
        if (consoleMessages.size > 64) {
            consoleMessages = consoleMessages.takeLast(64)
        }
    }
}

private fun (WebSettings.() -> Unit).toSettings(): WebSettings =
    WebSettings().apply { this@toSettings(this) }

@Composable
fun rememberWebViewState(
    url: String = "about:blank",
    additionalHttpHeaders: Map<String, String> = emptyMap(),
    interfaces: Map<String, Any> = emptyMap(),
    settings: WebSettings.() -> Unit = {},
) = remember(url, additionalHttpHeaders) {
    WebViewState(
        initialContent = WebContent.Url(url, additionalHttpHeaders),
        interfaces = interfaces,
        settings = settings
    )
}

@Composable
fun rememberWebViewState(
    data: String,
    baseUrl: String? = null,
    encoding: String = "utf-8",
    mimeType: String? = null,
    historyUrl: String? = null,
    interfaces: Map<String, Any> = emptyMap(),
    settings: WebSettings.() -> Unit = {},
) = remember(data, baseUrl, encoding, mimeType, historyUrl) {
    WebViewState(
        initialContent = WebContent.Data(data, baseUrl, encoding, mimeType, historyUrl),
        interfaces = interfaces,
        settings = settings
    )
}