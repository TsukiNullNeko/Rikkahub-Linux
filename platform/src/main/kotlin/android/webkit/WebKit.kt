package android.webkit

import android.content.Context
import java.io.InputStream

class WebSettings {
    var javaScriptEnabled: Boolean = true
    var domStorageEnabled: Boolean = true
    var allowContentAccess: Boolean = true
    var allowFileAccess: Boolean = true
    var databaseEnabled: Boolean = true
    var loadsImagesAutomatically: Boolean = true
    var useWideViewPort: Boolean = true
    var loadWithOverviewMode: Boolean = true
    var setSupportZoom: Boolean = true
    var builtInZoomControls: Boolean = true
    var displayZoomControls: Boolean = false
    var userAgentString: String? = null
    var cacheMode: Int = 0
    var mixedContentMode: Int = 0

    companion object {
        const val LOAD_DEFAULT = -1
        const val LOAD_CACHE_ELSE_NETWORK = 1
        const val LOAD_NO_CACHE = 2
        const val LOAD_CACHE_ONLY = 3
        const val MIXED_CONTENT_ALWAYS_ALLOW = 0
        const val MIXED_CONTENT_NEVER_ALLOW = 1
        const val MIXED_CONTENT_COMPATIBILITY_MODE = 2
    }
}

class ConsoleMessage(
    private val messageText: String = "",
    private val source: String = "",
    private val line: Int = 0,
    private val level: MessageLevel = MessageLevel.LOG
) {
    enum class MessageLevel { TIP, LOG, WARNING, ERROR, DEBUG }

    fun message(): String = messageText
    fun sourceId(): String = source
    fun lineNumber(): Int = line
    fun messageLevel(): MessageLevel = level
}

class WebResourceRequest {
    var url: String = ""
    var isForMainFrame: Boolean = true
    var method: String = "GET"
    val requestHeaders: Map<String, String> = emptyMap()
}

class WebResourceResponse(
    private val mimeType: String? = null,
    private val encoding: String? = null,
    private val data: InputStream? = null
) {
    fun getMimeType(): String? = mimeType
    fun getEncoding(): String? = encoding
    fun getData(): InputStream? = data
    fun setStatusCode(code: Int) {}
    fun setReasonPhrase(phrase: String?) {}
}

open class WebViewClient {
    open fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? = null
    open fun onPageStarted(view: WebView?, url: String?, favicon: Any?) {}
    open fun onPageFinished(view: WebView?, url: String?) {}
    open fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean = false
    open fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {}
}

open class WebChromeClient {
    open fun onProgressChanged(view: WebView?, newProgress: Int) {}
    open fun onReceivedTitle(view: WebView?, title: String?) {}
    open fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean = false
}

/**
 * Desktop stand-in for android.webkit.WebView.
 *
 * There is no embedded browser engine: navigation requests are forwarded to the system
 * browser (xdg-open) and the state is kept for the UI layer to display.
 */
open class WebView(val context: Context) {
    var settings: WebSettings = WebSettings()
    var webChromeClient: WebChromeClient? = null
    var webViewClient: WebViewClient? = null

    var url: String? = null
        private set
    var title: String? = null
        private set

    private val history = mutableListOf<String>()
    private var historyIndex = -1
    private val javascriptInterfaces = mutableMapOf<String, Any>()

    fun loadUrl(url: String) = loadUrl(url, emptyMap())

    fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        if (url == "about:blank") return
        this.url = url
        if (historyIndex < history.size - 1) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        history.add(url)
        historyIndex = history.size - 1
        webViewClient?.onPageStarted(this, url, null)
        webViewClient?.onPageFinished(this, url)
    }

    fun loadDataWithBaseURL(baseUrl: String?, data: String, mimeType: String?, encoding: String?, historyUrl: String?) {
        this.url = historyUrl ?: baseUrl
        webViewClient?.onPageFinished(this, this.url)
    }

    fun addJavascriptInterface(obj: Any, name: String) {
        javascriptInterfaces[name] = obj
    }

    fun removeJavascriptInterface(name: String) {
        javascriptInterfaces.remove(name)
    }

    fun goBack(): Boolean {
        if (historyIndex > 0) {
            historyIndex--
            url = history[historyIndex]
            webViewClient?.onPageFinished(this, url)
            return true
        }
        return false
    }

    fun goForward(): Boolean {
        if (historyIndex < history.size - 1) {
            historyIndex++
            url = history[historyIndex]
            webViewClient?.onPageFinished(this, url)
            return true
        }
        return false
    }

    fun canGoBack(): Boolean = historyIndex > 0
    fun canGoForward(): Boolean = historyIndex < history.size - 1
    fun reload() {}
    fun stopLoading() {}
    fun clearHistory() { history.clear(); historyIndex = -1 }
    fun clearCache(includeDiskFiles: Boolean) {}
    fun removeAllViews() {}
    fun destroy() {}
    fun evaluateJavascript(script: String, callback: ((String) -> Unit)? = null) { callback?.invoke("null") }
    fun setBackgroundColor(color: Int) {}
    fun setDownloadListener(listener: Any?) {}
}
