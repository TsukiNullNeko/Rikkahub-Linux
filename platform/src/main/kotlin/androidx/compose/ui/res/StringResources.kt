package androidx.compose.ui.res

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import android.content.AppContext

/**
 * Desktop implementation of stringResource(id: Int).
 * Reads from the string tables generated at build time and loaded by the app into
 * [AppContext.resources].
 */
@Composable
@ReadOnlyComposable
fun stringResource(id: Int): String = AppContext.resources.getString(id)

@Composable
@ReadOnlyComposable
fun stringResource(id: Int, vararg args: Any?): String = AppContext.resources.getString(id, *args)

@Composable
@ReadOnlyComposable
fun pluralStringResource(id: Int, count: Int, vararg args: Any?): String =
    AppContext.resources.getQuantityString(id, count, *args)
