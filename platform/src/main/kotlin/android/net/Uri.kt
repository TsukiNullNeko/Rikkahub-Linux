package android.net

import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

class Uri private constructor(private val uri: URI, private val opaquePath: String? = null) {
    val scheme: String? get() = uri.scheme
    val host: String? get() = uri.host
    val port: Int get() = if (uri.port == -1) -1 else uri.port
    val authority: String? get() = uri.rawAuthority
    val userInfo: String? get() = uri.rawUserInfo
    val query: String? get() = uri.rawQuery
    val fragment: String? get() = uri.rawFragment
    val isOpaque: Boolean get() = uri.isOpaque
    val isAbsolute: Boolean get() = uri.isAbsolute
    val isHierarchical: Boolean get() = !uri.isOpaque

    val path: String?
        get() = opaquePath ?: uri.path

    val pathSegments: List<String>
        get() = path?.split('/')?.filter { it.isNotEmpty() } ?: emptyList()

    val lastPathSegment: String?
        get() = pathSegments.lastOrNull()

    fun getQueryParameter(key: String): String? {
        val q = query ?: return null
        return q.split('&')
            .map { it.split('=', limit = 2) }
            .firstOrNull { URLDecoder.decode(it[0], Charsets.UTF_8) == key }
            ?.getOrNull(1)?.let { URLDecoder.decode(it, Charsets.UTF_8) }
    }

    fun getQueryParameters(key: String): List<String> {
        val q = query ?: return emptyList()
        return q.split('&')
            .map { it.split('=', limit = 2) }
            .filter { URLDecoder.decode(it[0], Charsets.UTF_8) == key }
            .mapNotNull { it.getOrNull(1)?.let { v -> URLDecoder.decode(v, Charsets.UTF_8) } }
    }

    fun getQueryParameterNames(): Set<String> {
        val q = query ?: return emptySet()
        return q.split('&').mapNotNull {
            val k = it.split('=', limit = 2)[0]
            if (k.isEmpty()) null else URLDecoder.decode(k, Charsets.UTF_8)
        }.toSet()
    }

    fun getBooleanQueryParameter(key: String, def: Boolean): Boolean =
        getQueryParameter(key)?.let { it == "true" || it == "1" } ?: def

    fun buildUpon(): Builder = Builder()
        .scheme(scheme)
        .encodedAuthority(authority)
        .encodedPath(path)
        .encodedQuery(query)
        .encodedFragment(fragment)

    override fun toString(): String = opaquePath ?: uri.toString()
    override fun equals(other: Any?): Boolean = other is Uri && other.toString() == toString()
    override fun hashCode(): Int = toString().hashCode()

    companion object {
        val EMPTY: Uri = parse("")

        fun parse(s: String): Uri {
            if (s.isEmpty()) return Uri(URI(""), "")
            val trimmed = s.trim()
            if (!trimmed.matches(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:.*$"))) {
                return Uri(URI("file", null, "/", null, null), trimmed)
            }
            return Uri(URI(trimmed))
        }

        fun fromFile(file: File): Uri = Uri(URI("file", null, file.absolutePath, null))

        fun encode(s: String): String = URLEncoder.encode(s, Charsets.UTF_8).replace("+", "%20")

        fun decode(s: String): String = URLDecoder.decode(s, Charsets.UTF_8)

        fun withAppendedPath(base: Uri, pathSegment: String): Uri =
            parse(buildString {
                append(base.toString())
                if (!endsWith("/")) append("/")
                append(encode(pathSegment))
            })
    }

    class Builder {
        private var scheme: String? = null
        private var opaque: String? = null
        private var authority: String? = null
        private var path: String? = null
        private var query: String? = null
        private var fragment: String? = null

        fun scheme(s: String?): Builder { scheme = s; return this }
        fun opaquePart(s: String?): Builder { opaque = s; return this }
        fun authority(s: String?): Builder { authority = s; return this }
        fun encodedAuthority(s: String?): Builder { authority = s; return this }
        fun path(s: String?): Builder { path = s; return this }
        fun encodedPath(s: String?): Builder { path = s; return this }
        fun appendPath(s: String?): Builder {
            val p = path ?: ""
            path = if (p.isEmpty()) "/" + Uri.encode(s ?: "") else "$p/${Uri.encode(s ?: "")}"
            return this
        }
        fun appendEncodedPath(s: String?): Builder {
            path = (path ?: "") + "/" + (s ?: "")
            return this
        }
        fun query(s: String?): Builder { query = s; return this }
        fun encodedQuery(s: String?): Builder { query = s; return this }
        fun fragment(s: String?): Builder { fragment = s; return this }
        fun encodedFragment(s: String?): Builder { fragment = s; return this }
        fun appendQueryParameter(key: String, value: String): Builder {
            val existing = query
            val pair = Uri.encode(key) + "=" + Uri.encode(value)
            query = if (existing.isNullOrEmpty()) pair else "$existing&$pair"
            return this
        }
        fun clearQuery(): Builder { query = null; return this }
        fun build(): Uri {
            val str = buildString {
                if (opaque != null) {
                    append(scheme ?: ""); append(":"); append(opaque)
                } else {
                    if (scheme != null) { append(scheme); append("://") }
                    if (authority != null) append(authority)
                    if (path != null) append(path)
                    if (query != null) { append("?"); append(query) }
                    if (fragment != null) { append("#"); append(fragment) }
                }
            }
            return Uri.parse(str)
        }
    }
}
