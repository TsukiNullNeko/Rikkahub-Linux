package androidx.lifecycle

import android.app.Application

/** Desktop stand-in for androidx AndroidViewModel (exposes the application context). */
abstract class AndroidViewModel(private val application: Application) : ViewModel() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Application> getApplication(): T = application as T
}
