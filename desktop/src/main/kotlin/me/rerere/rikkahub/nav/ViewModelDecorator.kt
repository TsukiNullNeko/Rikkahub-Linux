package androidx.lifecycle.viewmodel.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Provides a per-entry ViewModelStore plus SavedStateRegistryOwner (mirrors
 * lifecycle-viewmodel-navigation3) so SavedStateHandle-based ViewModels can resolve.
 */
class ViewModelStoreNavEntryDecorator<T : NavKey> : NavEntryDecorator<T> {
    @Composable
    override fun DecorateEntry(entry: NavEntry<T>) {
        val store = remember(entry.key) { ViewModelStore() }
        val owner = remember(store) { SimpleStoreOwner(store) }
        androidx.compose.runtime.CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            entry.content(entry.key)
        }
    }

    class SimpleStoreOwner(private val store: ViewModelStore) :
        ViewModelStoreOwner,
        SavedStateRegistryOwner,
        LifecycleOwner {

        // LifecycleRegistry 必须最先初始化：SavedStateRegistryController.create 会读取 owner.lifecycle
        private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)

        override val lifecycle: Lifecycle get() = lifecycleRegistry

        private val controller = SavedStateRegistryController.create(this).also { registry ->
            registry.performRestore(null)
            lifecycleRegistry.currentState = androidx.lifecycle.Lifecycle.State.CREATED
        }

        override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry

        override val viewModelStore: ViewModelStore get() = store

        init {
            lifecycleRegistry.currentState = androidx.lifecycle.Lifecycle.State.RESUMED
        }
    }
}

@Composable
fun <T : NavKey> rememberViewModelStoreNavEntryDecorator(): NavEntryDecorator<T> =
    remember { ViewModelStoreNavEntryDecorator<T>() }