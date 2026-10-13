package me.rerere.tts.controller

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import me.rerere.tts.model.AudioFormat
import me.rerere.tts.model.PlaybackState
import me.rerere.tts.model.PlaybackStatus
import me.rerere.tts.model.TTSResponse
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Desktop audio player for TTS responses.
 *
 * Android's media3/ExoPlayer is replaced with javax.sound.sampled; MP3/OGG decoding is
 * provided by the soundlibs SPI codecs on the classpath.
 */
class AudioPlayer(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var playJob: Job? = null
    private var positionJob: Job? = null
    private var line: SourceDataLine? = null
    private var paused = false
    private var speed = 1.0f
    private var durationMs = 0L
    private var positionMs = 0L

    fun pause() {
        paused = true
        runCatching { line?.stop() }
        _playbackState.update { it.copy(status = PlaybackStatus.Paused) }
    }

    fun resume() {
        paused = false
        runCatching { line?.start() }
        _playbackState.update { it.copy(status = PlaybackStatus.Playing) }
    }

    fun stop() {
        playJob?.cancel()
        positionJob?.cancel()
        runCatching {
            line?.stop()
            line?.flush()
            line?.close()
        }
        line = null
        _playbackState.update { it.copy(status = PlaybackStatus.Idle, positionMs = 0L) }
    }

    fun clear() {
        stop()
    }

    fun release() {
        stop()
    }

    fun seekBy(ms: Long) {
        positionMs = (positionMs + ms).coerceIn(0L, durationMs)
        _playbackState.update { it.copy(positionMs = positionMs) }
    }

    fun setSpeed(speed: Float) {
        this.speed = speed.coerceIn(0.25f, 4f)
        _playbackState.update { it.copy(speed = this.speed) }
    }

    suspend fun play(response: TTSResponse) = suspendCancellableCoroutine<Unit> { cont ->
        stop()

        val bytes = if (response.format == AudioFormat.PCM) {
            pcmToWav(response.audioData, response.sampleRate ?: 24000)
        } else {
            response.audioData
        }

        _playbackState.update {
            it.copy(
                status = PlaybackStatus.Buffering,
                positionMs = 0L,
                durationMs = (response.duration?.times(1000))?.toLong() ?: it.durationMs,
                speed = speed
            )
        }

        playJob = scope.launch {
            try {
                val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))
                val format = stream.format
                val info = DataLine.Info(SourceDataLine::class.java, format)
                val output = AudioSystem.getLine(info) as SourceDataLine
                output.open(format, 32 * 1024)
                output.start()
                line = output

                val frameSize = format.frameSize.coerceAtLeast(1)
                val frameRate = format.frameRate
                val totalFrames = stream.frameLength
                durationMs = if (frameRate > 0 && totalFrames > 0) (totalFrames / frameRate * 1000).toLong() else durationMs

                _playbackState.update {
                    it.copy(
                        status = PlaybackStatus.Playing,
                        durationMs = durationMs,
                        positionMs = 0L
                    )
                }
                startPositionUpdates()

                val buffer = ByteArray(8192)
                var framesPlayed = 0L
                var read = stream.read(buffer)
                while (read > 0) {
                    if (!cont.isActive) break
                    while (paused) {
                        delay(50)
                        if (!cont.isActive) break
                    }
                    val effective = if (speed == 1.0f) buffer.copyOf(read) else resample(buffer, read, frameSize, speed)
                    output.write(effective, 0, effective.size)
                    framesPlayed += read / frameSize
                    positionMs = if (frameRate > 0) (framesPlayed / frameRate * 1000).toLong() else positionMs
                    read = stream.read(buffer)
                }
                output.drain()
                output.stop()
                output.close()
                line = null
                stopPositionUpdates()
                if (cont.isActive) {
                    _playbackState.update {
                        it.copy(status = PlaybackStatus.Ended, positionMs = it.durationMs)
                    }
                    cont.resume(Unit)
                }
            } catch (e: Exception) {
                stopPositionUpdates()
                android.util.Log.e("AudioPlayer", "playback failed", e)
                _playbackState.update {
                    it.copy(status = PlaybackStatus.Error, errorMessage = e.message ?: "playback failed")
                }
                if (cont.isActive) cont.resumeWithException(e)
            }
        }

        cont.invokeOnCancellation {
            playJob?.cancel()
            stopPositionUpdates()
            runCatching {
                line?.stop()
                line?.close()
            }
            line = null
        }
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (true) {
                _playbackState.update { it.copy(positionMs = positionMs) }
                delay(100)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    /** Naive linear-interpolation resampler used for playback speed control. */
    private fun resample(buffer: ByteArray, length: Int, frameSize: Int, speed: Float): ByteArray {
        if (frameSize != 2) return buffer.copyOf(length)
        val inFrames = length / 2
        val outFrames = (inFrames / speed).toInt().coerceAtLeast(1)
        val out = ByteArray(outFrames * 2)
        for (i in 0 until outFrames) {
            val srcPos = i * speed
            val i0 = srcPos.toInt().coerceIn(0, inFrames - 1)
            val i1 = (i0 + 1).coerceAtMost(inFrames - 1)
            val frac = srcPos - i0
            val s0 = ((buffer[i0 * 2 + 1].toInt() shl 8) or (buffer[i0 * 2].toInt() and 0xFF))
            val s1 = ((buffer[i1 * 2 + 1].toInt() shl 8) or (buffer[i1 * 2].toInt() and 0xFF))
            val sample = (s0 + (s1 - s0) * frac).toInt().coerceIn(-32768, 32767)
            out[i * 2] = (sample and 0xFF).toByte()
            out[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
        }
        return out
    }

    private fun pcmToWav(
        pcm: ByteArray,
        sampleRate: Int,
        channels: Int = 1,
        bitsPerSample: Int = 16
    ): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val out = ByteArrayOutputStream()
        with(out) {
            write("RIFF".toByteArray())
            write(intToBytes(36 + pcm.size))
            write("WAVE".toByteArray())
            write("fmt ".toByteArray())
            write(intToBytes(16))
            write(shortToBytes(1))
            write(shortToBytes(channels.toShort()))
            write(intToBytes(sampleRate))
            write(intToBytes(byteRate))
            write(shortToBytes((channels * bitsPerSample / 8).toShort()))
            write(shortToBytes(bitsPerSample.toShort()))
            write("data".toByteArray())
            write(intToBytes(pcm.size))
            write(pcm)
        }
        return out.toByteArray()
    }

    private fun intToBytes(value: Int) = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun shortToBytes(value: Short) = byteArrayOf(
        (value.toInt() and 0xFF).toByte(),
        ((value.toInt() shr 8) and 0xFF).toByte()
    )
}
