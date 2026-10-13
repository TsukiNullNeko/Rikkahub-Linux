package androidx.datastore.preferences

import android.content.Context
import java.io.File

/** Mirrors the Android preferencesDataStoreFile helper using the app config directory. */
fun Context.preferencesDataStoreFile(name: String): File {
    val dir = android.content.AppContext.configDir.resolve("datastore")
    dir.mkdirs()
    return File(dir, "$name.preferences_pb")
}
