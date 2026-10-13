package androidx.lifecycle.viewmodel.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey

/** Provides a per-entry ViewModelStore, mirroring lifecycle-viewmodel-navigation3. */
class ViewModelStoreNavEntryDecorator<T : NavKey> : NavEntryDecorator<T> {
    @Composable
    override fun DecorateEntry(entry: NavEntry<T>) {
        val store = remember(entry.key) { ViewModelStore() }
        val owner = remember(store) { SimpleStoreOwner(store) }
        androidx.compose.runtime.CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            entry.content(entry.key)
        }
    }

    private class SimpleStoreOwner(private val store: ViewModelStore) : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore get() = store
    }
}

@Composable
fun <T : NavKey> rememberViewModelStoreNavEntryDecorator(): NavEntryDecorator<T> =
    remember { ViewModelStoreNavEntryDecorator<T>() }
