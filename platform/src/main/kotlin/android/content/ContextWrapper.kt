package android.content

open class ContextWrapper(private val base: Context) : Context() {
    override val packageName: String get() = base.packageName
    override val filesDir: java.io.File get() = base.filesDir
    override val cacheDir: java.io.File get() = base.cacheDir
    override val assets: android.content.res.AssetManager get() = base.assets
    override val resources: android.content.res.Resources get() = base.resources
    override val contentResolver: ContentResolver get() = base.contentResolver
    override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = base.getSharedPreferences(name, mode)
    override fun getSystemService(name: String): Any? = base.getSystemService(name)
    override fun startActivity(intent: Intent) = base.startActivity(intent)
    override fun checkSelfPermission(permission: String): Int = base.checkSelfPermission(permission)
}
