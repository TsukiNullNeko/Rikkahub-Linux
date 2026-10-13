package me.rerere.ui.webview

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse

/**
 * 应用内 WebView 使用的虚拟域名，用于给 loadDataWithBaseURL 提供一个稳定的 origin。
 */
const val WEB_VIEW_BASE_URL = "https://rikkahub.local"

/**
 * 虚拟域名下的静态资源前缀，会被映射到 app 的 assets 目录。
 */
const val WEB_VIEW_ASSET_URL = "$WEB_VIEW_BASE_URL/assets"

private const val ASSET_HOST = "rikkahub.local"
private const val ASSET_PATH_PREFIX = "/assets/"

internal object WebViewLocalAssets {
    fun intercept(context: Context, uri: Uri): WebResourceResponse? {
        if (!uri.host.equals(ASSET_HOST, ignoreCase = true)) return null
        val path = uri.path ?: return null
        if (!path.startsWith(ASSET_PATH_PREFIX)) return null

        val assetPath = path.removePrefix(ASSET_PATH_PREFIX)
        if (assetPath.isEmpty() || assetPath.contains("..")) return null

        val mimeType = mimeTypeOf(assetPath)
        return runCatching {
            WebResourceResponse(
                mimeType,
                if (mimeType.startsWith("text/") || mimeType.endsWith("json") || mimeType.endsWith("xml")) "UTF-8" else null,
                context.assets.open(assetPath)
            )
        }.getOrNull()
    }

    private fun mimeTypeOf(path: String): String {
        val extension = path.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "html", "htm" -> "text/html"
            "js", "mjs" -> "application/javascript"
            "css" -> "text/css"
            "json" -> "application/json"
            "svg" -> "image/svg+xml"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            "ttf" -> "font/ttf"
            "wasm" -> "application/wasm"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}
