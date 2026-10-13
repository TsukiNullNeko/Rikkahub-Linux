package me.rerere.rikkahub.ui.pages.extensions.workspace

import android.content.Context
import android.util.Log
import me.rerere.rikkahub.data.files.FileFolders
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

private const val TAG = "WorkspaceTerminal"

/**
 * Desktop terminal session.
 *
 * The Android build runs a proot rootfs through Termux's terminal emulator; the Linux build
 * runs the host shell inside the workspace directory instead.
 */
class WorkspaceTerminalSession(
    val id: Long,
    val workingDir: File,
    private val onOutput: (WorkspaceTerminalSession) -> Unit,
    private val onTitleUpdated: (WorkspaceTerminalSession, String?) -> Unit,
    private val onFinished: (WorkspaceTerminalSession) -> Unit,
) {
    private val outputLock = Any()
    private val outputBuffer = StringBuilder()
    private val outputListeners = CopyOnWriteArrayList<(String) -> Unit>()

    private val process: Process? = startShell()
    private var readerThread: Thread? = null

    @Volatile
    var finished: Boolean = false
        private set

    @Volatile
    var title: String? = null
        private set

    private fun startShell(): Process? {
        return try {
            workingDir.mkdirs()
            val shell = System.getenv("SHELL")?.takeIf { it.isNotBlank() } ?: "/bin/bash"
            ProcessBuilder(shell, "-i")
                .directory(workingDir)
                .redirectErrorStream(true)
                .apply {
                    environment()["TERM"] = "xterm-256color"
                    environment()["PS1"] = "\\w $ "
                }
                .start()
                .also { started ->
                    readerThread = Thread({ pumpOutput(started) }, "terminal-reader-$id").apply {
                        isDaemon = true
                        start()
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "failed to start shell", e)
            appendOutput("Failed to start shell: " + (e.message ?: "unknown error") + "\n")
            null
        }
    }

    private fun pumpOutput(started: Process) {
        try {
            val buffer = ByteArray(4096)
            val input = started.inputStream
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                appendOutput(String(buffer, 0, read, Charsets.UTF_8))
            }
        } catch (e: Exception) {
            Log.d(TAG, "shell output stream closed: " + e.message)
        } finally {
            finished = true
            onFinished(this)
        }
    }

    private fun appendOutput(text: String) {
        synchronized(outputLock) {
            outputBuffer.append(text)
            if (outputBuffer.length > 200_000) {
                outputBuffer.delete(0, outputBuffer.length - 200_000)
            }
        }
        outputListeners.forEach { it(text) }
        onOutput(this)
    }

    fun snapshot(): String = synchronized(outputLock) { outputBuffer.toString() }

    fun addOutputListener(listener: (String) -> Unit) {
        outputListeners.add(listener)
    }

    fun removeOutputListener(listener: (String) -> Unit) {
        outputListeners.remove(listener)
    }

    fun write(text: String) {
        val running = process ?: return
        try {
            running.outputStream.write(text.toByteArray(Charsets.UTF_8))
            running.outputStream.flush()
        } catch (e: Exception) {
            Log.e(TAG, "failed to write to shell", e)
        }
    }

    fun sendCommand(command: String) {
        appendOutput("$ " + command + "\n")
        write(command + "\n")
    }

    fun interrupt() {
        process?.destroy()
    }

    fun resize(columns: Int, rows: Int) {
        // A pipe-based shell has no window size; kept for API compatibility.
    }

    fun updateTitle(newTitle: String?) {
        title = newTitle
        onTitleUpdated(this, newTitle)
    }

    fun destroy() {
        try {
            process?.destroy()
        } catch (_: Exception) {
        }
        finished = true
    }
}

/** Prepares the workspace directory that hosts the shell. */
internal fun prepareWorkspaceTerminalSession(context: Context, root: String) {
    val workspaceDir = File(File(context.filesDir, "workspaces"), root)
    File(workspaceDir, "files").mkdirs()
    File(workspaceDir, "tmp").mkdirs()
    File(context.filesDir, FileFolders.SKILLS).mkdirs()
}

/** On Linux the "rootfs" is simply the workspace directory. */
internal fun workspaceRootfsReady(context: Context, root: String): Boolean {
    val workspaceDir = File(File(context.filesDir, "workspaces"), root)
    return workspaceDir.exists() || workspaceDir.mkdirs()
}

internal fun workspaceTerminalWorkingDir(context: Context, root: String): File =
    File(File(File(context.filesDir, "workspaces"), root), "files").also { it.mkdirs() }
