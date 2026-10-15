package me.rerere.rikkahub.res

import android.content.res.Resources
import java.util.Locale

/**
 * Merges the build-time generated string tables of every module and selects the
 * best matching locale, mirroring Android's resource resolution.
 */
object RStringRegistry {
    private var cachedLocale: String? = null
    private var cached: Map<Int, String> = emptyMap()

    fun register(locale: Locale) {
        val key = locale.toLanguageTag()
        if (key == cachedLocale) {
            Resources.stringsById = cached
            return
        }
        val merged = mergeForLocale(locale)
        cached = merged
        cachedLocale = key
        Resources.stringsById = merged
    }

    private fun mergeForLocale(locale: Locale): Map<Int, String> {
        val lang = locale.language.lowercase()
        val region = locale.country.uppercase()
        val specific = "$lang-r$region"

        val result = HashMap<Int, String>(4096)

        fun add(default: Map<Int, String>, byLocale: Map<String, Map<Int, String>>) {
            result.putAll(default)
            val localized = byLocale[specific] ?: byLocale[lang]
            if (localized != null) {
                result.putAll(localized)
            }
        }

        add(me.rerere.rikkahub.RStringTable.default, me.rerere.rikkahub.RStringTable.byLocale)
        add(me.rerere.ui.RStringTable.default, me.rerere.ui.RStringTable.byLocale)
        add(me.rerere.search.RStringTable.default, me.rerere.search.RStringTable.byLocale)
        add(me.rerere.oauth.RStringTable.default, me.rerere.oauth.RStringTable.byLocale)

        // Framework strings (android.R.string.*)
        result[android.R.string.ok] = "OK"
        result[android.R.string.cancel] = "Cancel"
        result[android.R.string.yes] = "Yes"
        result[android.R.string.no] = "No"
        result[android.R.string.copy] = "Copy"
        result[android.R.string.cut] = "Cut"
        result[android.R.string.paste] = "Paste"
        result[android.R.string.selectAll] = "Select all"
        result[android.R.string.delete] = "Delete"
        result[android.R.string.share] = "Share"
        result[android.R.string.close] = "Close"
        result[android.R.string.loading] = "Loading…"

        return result
    }
}