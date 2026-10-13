package me.rerere.tts.provider.providers

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.rerere.tts.model.AudioChunk
import me.rerere.tts.model.AudioFormat
import me.rerere.tts.model.TTSRequest
import me.rerere.tts.provider.TTSProvider
import me.rerere.tts.provider.TTSProviderSetting
import java.io.File
import java.util.concurrent.TimeUnit

private const val TAG = "SystemTTSProvider"

/**
 * Desktop "system" TTS provider.
 *
 * Android's TextToSpeech engine is replaced by a local speech synthesizer binary.
 * Supported backends (first one found wins): espeak-ng, espeak, spd-say(no file output -> skipped).
 */
class SystemTTSProvider : TTSProvider<TTSProviderSetting.SystemTTS> {
    override fun generateSpeech(
        context: Context,
        providerSetting: TTSProviderSetting.SystemTTS,
        request: TTSRequest
    ): Flow<AudioChunk> = flow {
        val audioData = withContext(Dispatchers.IO) {
            synthesize(context, providerSetting, request.text)
        }
        emit(
            AudioChunk(
                data = audioData,
                format = AudioFormat.WAV,
                isLast = true,
                metadata = mapOf(
                    "provider" to "system",
                    "speechRate" to providerSetting.speechRate.toString(),
                    "pitch" to providerSetting.pitch.toString()
                )
            )
        )
    }

    private fun synthesize(
        context: Context,
        setting: TTSProviderSetting.SystemTTS,
        text: String
    ): ByteArray {
        val engine = findEngine()
            ?: throw IllegalStateException(
                "No system speech synthesizer found. Install espeak-ng (sudo apt install espeak-ng) to use System TTS."
            )

        val tempDir = context.cacheDir
        tempDir.mkdirs()
        val audioFile = File(tempDir, "tts_" + System.currentTimeMillis() + ".wav")

        try {
            // espeak-ng: -s words per minute (default 175), -p pitch (0-99)
            val wpm = (175 * setting.speechRate).toInt().coerceIn(80, 450)
            val pitch = (50 * setting.pitch).toInt().coerceIn(0, 99)
            val command = listOf(
                engine, "-w", audioFile.absolutePath,
                "-s", wpm.toString(),
                "-p", pitch.toString(),
                text
            )
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            val finished = process.waitFor(120, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                throw IllegalStateException("system TTS timed out")
            }
            if (process.exitValue() != 0 || !audioFile.exists()) {
                val output = process.inputStream.bufferedReader().readText()
                throw IllegalStateException("system TTS failed: " + output.take(400))
            }
            return audioFile.readBytes()
        } finally {
            audioFile.delete()
        }
    }

    private fun findEngine(): String? {
        val candidates = listOf("espeak-ng", "espeak")
        for (candidate in candidates) {
            try {
                val process = ProcessBuilder("which", candidate)
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText().trim()
                process.waitFor(5, TimeUnit.SECONDS)
                if (output.isNotEmpty()) return output
            } catch (_: Exception) {
            }
        }
        return null
    }
}
