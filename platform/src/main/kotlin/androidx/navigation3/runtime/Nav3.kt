package androidx.navigation3.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.Serializable

/** Marker interface for navigation keys. */
interface NavKey : java.io.Serializable

/** A navigation entry with its key, optional metadata and content composable. */
class NavEntry<T : Any>(
    val key: T,
    val metadata: Map<String, Any> = emptyMap(),
    val content: @Composable (T) -> Unit
)

/** Provides the [NavEntry] for a given key. */
fun interface EntryProvider<T : Any> {
    fun getEntry(key: T): NavEntry<T>?
}

class EntryProviderBuilder<T : Any> @PublishedApi internal constructor() {
    @PublishedApi
    internal val builders = mutableListOf<(T) -> NavEntry<T>?>()

    inline fun <reified K : T> entry(
        metadata: Map<String, Any> = emptyMap(),
        noinline content: @Composable (K) -> Unit
    ) {
        val contentFn = content
        builders.add { key ->
            // Avoid unchecked casts of composable lambdas: call the typed function directly.
            if (key::class == K::class) {
                @Suppress("UNCHECKED_CAST")
                NavEntry(key, metadata) { k -> contentFn(k as K) }
            } else {
                null
            }
        }
    }

    @PublishedApi
    internal fun build(): EntryProvider<T> = EntryProvider { key ->
        builders.firstNotNullOfOrNull { it(key) }
    }
}

fun <T : Any> entryProvider(block: EntryProviderBuilder<T>.() -> Unit): EntryProvider<T> {
    return EntryProviderBuilder<T>().apply(block).build()
}

/** A mutable, observable back stack of [NavKey]s. */
class NavBackStack(initialKeys: List<NavKey>) : MutableList<NavKey> by initialKeys.toMutableStateList() {
    operator fun plusAssign(key: NavKey) { add(key) }
    fun setTop(key: NavKey) {
        if (isNotEmpty()) this[size - 1] = key else add(key)
    }
}

@Composable
fun rememberNavBackStack(vararg initialKeys: NavKey): NavBackStack {
    return rememberSaveable(saver = NavBackStackSaver) {
        NavBackStack(initialKeys.toList())
    }
}

object NavBackStackSaver : androidx.compose.runtime.saveable.Saver<NavBackStack, List<NavKey>> {
    override fun androidx.compose.runtime.saveable.SaverScope.save(value: NavBackStack): List<NavKey> =
        value.toList()

    override fun restore(value: List<NavKey>): NavBackStack = NavBackStack(value)
}

// Alias used by some call sites
typealias NavEntryProviderBuilder<T> = EntryProviderBuilder<T>
