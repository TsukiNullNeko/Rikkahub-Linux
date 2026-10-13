package me.rerere.rikkahub.ui.components.ui.permission

import android.app.Activity
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Desktop permission state.
 *
 * Linux has no runtime permission prompts: microphone and file access are controlled by the
 * desktop environment, so every declared permission reports as granted and requests are no-ops.
 */
@Stable
class PermissionState internal constructor(
    private val permissions: Set<PermissionInfo>,
    private val context: Context,
    private val activity: Activity
) {
    private val _permissionStates = mutableStateMapOf<String, PermissionStatus>()
    val permissionStates: Map<String, PermissionStatus> = _permissionStates

    var showRationaleDialog by mutableStateOf(false)
        private set

    var currentRationalePermissions by mutableStateOf<List<PermissionInfo>>(emptyList())
        private set

    private var permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>? = null
    private var singlePermissionLauncher: ManagedActivityResultLauncher<String, Boolean>? = null

    init {
        permissions.forEach { _permissionStates[it.permission] = PermissionStatus.Granted }
    }

    internal fun setPermissionLaunchers(
        permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>?,
        singlePermissionLauncher: ManagedActivityResultLauncher<String, Boolean>?
    ) {
        this.permissionLauncher = permissionLauncher
        this.singlePermissionLauncher = singlePermissionLauncher
    }

    fun updatePermissionStates() {
        permissions.forEach { _permissionStates[it.permission] = PermissionStatus.Granted }
    }

    val allPermissionsGranted: Boolean
        get() = permissions.all { _permissionStates[it.permission] == PermissionStatus.Granted }

    val allRequiredPermissionsGranted: Boolean
        get() = permissions.filter { it.required }.all {
            _permissionStates[it.permission] == PermissionStatus.Granted
        }

    val deniedPermissions: List<PermissionInfo>
        get() = permissions.filter { _permissionStates[it.permission] != PermissionStatus.Granted }

    val permanentlyDeniedPermissions: List<PermissionInfo>
        get() = emptyList()

    fun requestPermissions() {
        updatePermissionStates()
    }

    fun requestPermission(permission: String) {
        updatePermissionStates()
    }

    fun proceedFromRationale() {
        showRationaleDialog = false
        currentRationalePermissions = emptyList()
        updatePermissionStates()
    }

    fun cancelPermissionRequest() {
        showRationaleDialog = false
        currentRationalePermissions = emptyList()
    }

    fun openAppSettings() {
        // Linux has no per-app permission settings screen.
    }

    fun refreshPermissionStates() {
        updatePermissionStates()
    }

    fun handlePermissionResult(permissions: Array<String>, grantResults: IntArray) {
        updatePermissionStates()
    }

    fun handlePermissionResult(permissions: Array<String>, grantResults: Map<String, Boolean>) {
        updatePermissionStates()
    }

    fun handleSinglePermissionResult(permission: String, granted: Boolean) {
        updatePermissionStates()
    }

    fun isGranted(permission: String): Boolean =
        _permissionStates[permission] == PermissionStatus.Granted
}