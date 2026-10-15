package androidx.activity.result

/** Android exposes PickVisualMediaRequest in androidx.activity.result (not .contract). */
typealias PickVisualMediaRequest = androidx.activity.result.contract.PickVisualMediaRequest

typealias ActivityResultLauncher<I> = androidx.activity.compose.ManagedActivityResultLauncher<I, Any?>
