package androidx.activity.compose

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class ManagedActivityResultLauncher<I : Any?, O : Any?>(
    val contract: ActivityResultContract<I, O>,
    private val onResult: (O) -> Unit,
    private val context: Context?
) {
    fun launch(input: I, options: Any? = null) {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = contract.execute(input, context)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onResult(result)
                }
            } catch (e: Exception) {
                android.util.Log.e("Launcher", "activity result failed", e)
            }
        }
    }

    fun launch() = launch(null as I)
}

@Composable
fun <I : Any?, O : Any?> rememberLauncherForActivityResult(
    contract: ActivityResultContract<I, O>,
    onResult: (O) -> Unit
): ManagedActivityResultLauncher<I, O> {
    val context = LocalContext.current
    return remember(contract) { ManagedActivityResultLauncher(contract, onResult, context) }
}