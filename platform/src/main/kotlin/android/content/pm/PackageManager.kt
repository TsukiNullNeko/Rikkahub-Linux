package android.content.pm

import android.content.Context

class PackageManager {
    companion object {
        const val PERMISSION_GRANTED = 0
        const val PERMISSION_DENIED = -1
        const val GET_META_DATA = 128
    }

    class PackageInfo {
        var versionName: String? = "1.0"
        var versionCode: Long = 1
        var lastUpdateTime: Long = System.currentTimeMillis()
        var firstInstallTime: Long = System.currentTimeMillis()
        var packageName: String = "me.rerere.rikkahub"
    }

    fun getPackageInfo(pkg: String, flags: Int): PackageInfo = PackageInfo()

    fun checkPermission(permission: String, pkg: String): Int = PERMISSION_GRANTED

    fun queryIntentActivities(intent: android.content.Intent, flags: Int): List<ResolveInfo> = emptyList()

    fun getLaunchIntentForPackage(pkg: String): android.content.Intent? = null

    fun setComponentEnabledSetting(component: ComponentName, newState: Int, flags: Int) {}

    fun hasSystemFeature(feature: String): Boolean = false

    fun getApplicationLabel(info: ApplicationInfo): CharSequence = info.packageName

    fun getApplicationInfo(packageName: String, flags: Int): ApplicationInfo = ApplicationInfo()

    fun resolveActivity(intent: android.content.Intent, flags: Int): ResolveInfo? = null

    fun getInstalledApplications(flags: Int): List<ApplicationInfo> = emptyList()

    fun getInstalledPackages(flags: Int): List<PackageInfo> = emptyList()

    fun getInstallerPackageName(packageName: String): String? = null

    fun getInstallSourceInfo(packageName: String): InstallSourceInfo = InstallSourceInfo()

    fun getLaunchIntentForPackage(pkg: String, flags: Int): android.content.Intent? = null
}

class InstallSourceInfo {
    var initiatingPackageName: String? = null
    var installingPackageName: String? = null
    var originatingPackageName: String? = null
    var packageSourceInfo: String? = null
}

class ResolveInfo {
    var labelRes: Int = 0
    var packageName: String? = null
    var activityInfo: android.app.ActivityInfo? = android.app.ActivityInfo()
    var serviceInfo: ServiceInfo? = null
    var nonLocalizedLabel: CharSequence? = null
    var icon: Int = 0
    var priority: Int = 0
    var match: Int = 0
}

class ApplicationInfo {
    var packageName: String = ""
    var name: String? = null
    var labelRes: Int = 0
    var nonLocalizedLabel: CharSequence? = null
    var nativeLibraryDir: String = ""
    var dataDir: String = android.content.AppContext.dataDir.absolutePath
    var flags: Int = 0
}

object ServiceInfo {
    const val FOREGROUND_SERVICE_TYPE_MICROPHONE = 128
    const val FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK = 2
    const val FOREGROUND_SERVICE_TYPE_SPECIAL_USE = 1073741824
    const val FOREGROUND_SERVICE_TYPE_DATA_SYNC = 1
    const val FOREGROUND_SERVICE_TYPE_LOCATION = 8
}

class ComponentName(pkg: String, cls: String) {
    val packageName: String = pkg
    val className: String = cls
}