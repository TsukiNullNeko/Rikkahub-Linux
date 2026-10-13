package androidx.navigation3.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.EntryProvider

/** Per-entry transition metadata keys, mirroring navigation3. */
const val TRANSITION_SPEC_KEY = "navigation3.transitionSpec"
const val POP_TRANSITION_SPEC_KEY = "navigation3.popTransitionSpec"
const val PREDICTIVE_POP_TRANSITION_SPEC_KEY = "navigation3.predictivePopTransitionSpec"

/** Scope provided to entry content, mirroring navigation3's LocalNavAnimatedContentScope. */
val LocalNavAnimatedContentScope: androidx.compose.runtime.ProvidableCompositionLocal<AnimatedVisibilityScope?> =
    compositionLocalOf { null }

/**
 * Desktop implementation of navigation3's NavDisplay: renders the top of the back stack
 * with animated transitions and per-entry decorators.
 */
@Composable
fun <T : NavKey> NavDisplay(
    backStack: List<T>,
    modifier: Modifier = Modifier,
    onBack: (Int) -> Unit = {},
    entryDecorators: List<NavEntryDecorator<T>> = emptyList(),
    transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = {
        fadeIn() togetherWith fadeOut()
    },
    popTransitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = transitionSpec,
    predictivePopTransitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = popTransitionSpec,
    entryProvider: EntryProvider<T>,
    contentAlignment: Alignment = Alignment.TopStart
) {
    val top: T = backStack.lastOrNull() ?: return

    androidx.activity.compose.EscBackHandler(enabled = backStack.size > 1, onBack = { onBack(1) })

    AnimatedContent(
        targetState = top,
        modifier = modifier,
        transitionSpec = transitionSpec,
        label = "NavDisplay",
        contentAlignment = contentAlignment
    ) { key ->
        val entry = entryProvider.getEntry(key) ?: return@AnimatedContent
        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
            var content: @Composable () -> Unit = { entry.content(entry.key) }
            for (decorator in entryDecorators.asReversed()) {
                val inner = content
                val d = decorator
                content = { d.DecorateEntry(NavEntry(entry.key, entry.metadata) { inner() }) }
            }
            content()
        }
    }
}

/**
 * Companion-like holder for the per-entry transition metadata helpers
 * (NavDisplay.transitionSpec { ... } etc.).
 */
object NavDisplay {
    fun transitionSpec(
        block: AnimatedContentTransitionScope<NavKey>.() -> ContentTransform
    ): Map<String, Any> = mapOf(TRANSITION_SPEC_KEY to block)

    fun popTransitionSpec(
        block: AnimatedContentTransitionScope<NavKey>.() -> ContentTransform
    ): Map<String, Any> = mapOf(POP_TRANSITION_SPEC_KEY to block)

    fun predictivePopTransitionSpec(
        block: AnimatedContentTransitionScope<NavKey>.() -> ContentTransform
    ): Map<String, Any> = mapOf(PREDICTIVE_POP_TRANSITION_SPEC_KEY to block)
}