package org.koin.android.ext.android

import android.content.Context
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/** Desktop equivalents of the Koin Android extensions, working over any Context. */
fun Context.getKoin(): org.koin.core.Koin = GlobalContext.get()

inline fun <reified T : Any> Context.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = getKoin().get(qualifier, parameters)

inline fun <reified T : Any> Context.inject(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> = lazy(mode) { get<T>(qualifier, parameters) }
