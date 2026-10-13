package androidx.core.app

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

object ActivityCompat {
    const val PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED
    const val PERMISSION_DENIED = PackageManager.PERMISSION_DENIED

    fun requestPermissions(activity: Activity?, permissions: Array<String>, requestCode: Int) {}

    fun shouldShowRequestPermissionRationale(activity: Activity?, permission: String): Boolean = false

    fun checkSelfPermission(context: Context, permission: String): Int = PackageManager.PERMISSION_GRANTED

    fun recreate(activity: Activity) {}

    fun startActivity(context: Context, intent: android.content.Intent) = context.startActivity(intent)

    fun finishAffinity(activity: Activity) {}
}
