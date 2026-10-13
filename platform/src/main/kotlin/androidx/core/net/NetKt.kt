package androidx.core.net

import android.net.Uri
import java.io.File

fun String.toUri(): Uri = Uri.parse(this)
fun Uri.toFile(): File {
    check("file" == scheme) { "Uri is not a file URI: " + this }
    return File(path!!)
}
fun File.toUri(): Uri = Uri.fromFile(this)
