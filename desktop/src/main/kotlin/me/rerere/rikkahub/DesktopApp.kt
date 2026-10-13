package me.rerere.rikkahub

import android.content.AppContext
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import me.rerere.rikkahub.di.appModule
import me.rerere.rikkahub.di.dataSourceModule
import me.rerere.rikkahub.di.repositoryModule
import me.rerere.rikkahub.di.viewModelModule
import me.rerere.rikkahub.res.RStringRegistry
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.Locale

private const val TAG = "RikkaHub"

const val CHAT_COMPLETED_NOTIFICATION_CHANNEL_ID = "chat_completed"
const val CHAT_LIVE_UPDATE_NOTIFICATION_CHANNEL_ID = "chat_live_update"
const val WEB_SERVER_NOTIFICATION_CHANNEL_ID = "web_server"
const val MEDIA_CREATION_NOTIFICATION_CHANNEL_ID = "media_creation"
const val MEMORY_DREAM_NOTIFICATION_CHANNEL_ID = "memory_dream"

class AppScope : CoroutineScope by CoroutineScope(
    SupervisorJob()
        + Dispatchers.Default
        + CoroutineName("AppScope")
        + CoroutineExceptionHandler { _, e ->
            Log.e(TAG, "AppScope exception", e)
        }
)

/** Build-time generated application metadata (replaces the Android BuildConfig). */
object BuildConfig {
    const val DEBUG = false
    const val APPLICATION_ID = "me.rerere.rikkahub"
    const val BUILD_TYPE = "release"
    const val VERSION_NAME = "1.0.0-linux"
    const val VERSION_CODE = 1
}

/**
 * Desktop application bootstrap: loads resources, starts dependency injection and
 * prepares the data directories.
 */
object RikkaHubApp {
    @Volatile
    private var started = false

    val appScope: AppScope by lazy { AppScope() }

    fun init() {
        if (started) return
        started = true

        // Load localized strings into the resource shim
        RStringRegistry.register(Locale.getDefault())

        startKoin {
            modules(appModule, viewModelModule, dataSourceModule, repositoryModule)
        }

        Log.i(TAG, "RikkaHub started, data dir: " + AppContext.dataDir.absolutePath)
    }

    fun shutdown() {
        runCatching { appScope.cancel() }
    }
}
