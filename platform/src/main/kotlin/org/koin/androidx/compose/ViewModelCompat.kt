package org.koin.androidx.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/**
 * Desktop shim for org.koin.androidx.compose.koinViewModel, forwarding to the
 * multiplatform koin-compose-viewmodel implementation (which supplies the
 * ViewModelStoreOwner / CreationExtras / Scope defaults).
 */
@Composable
inline fun <reified VM : ViewModel> koinViewModel(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): VM = org.koin.compose.viewmodel.koinViewModel(
    qualifier = qualifier,
    parameters = parameters
)

@Composable
inline fun <reified VM : ViewModel> koinViewModel(
    viewModelStoreOwner: androidx.lifecycle.ViewModelStoreOwner,
    key: String? = null,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): VM = org.koin.compose.viewmodel.koinViewModel(
    qualifier = qualifier,
    viewModelStoreOwner = viewModelStoreOwner,
    key = key,
    parameters = parameters
)
