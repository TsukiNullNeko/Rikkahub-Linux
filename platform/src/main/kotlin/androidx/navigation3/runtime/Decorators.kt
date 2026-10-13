package androidx.navigation3.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder

/** Decorates how a [NavEntry] is rendered inside [androidx.navigation3.ui.NavDisplay]. */
interface NavEntryDecorator<T : NavKey> {
    @Composable
    fun DecorateEntry(entry: NavEntry<T>)
}

class SaveableStateHolderNavEntryDecorator<T : NavKey>(
    private val stateHolder: SaveableStateHolder
) : NavEntryDecorator<T> {
    @Composable
    override fun DecorateEntry(entry: NavEntry<T>) {
        stateHolder.SaveableStateProvider(entry.key) {
            entry.content(entry.key)
        }
    }
}

@Composable
fun <T : NavKey> rememberSaveableStateHolderNavEntryDecorator(): NavEntryDecorator<T> {
    val holder = rememberSaveableStateHolder()
    return remember(holder) { SaveableStateHolderNavEntryDecorator(holder) }
}
