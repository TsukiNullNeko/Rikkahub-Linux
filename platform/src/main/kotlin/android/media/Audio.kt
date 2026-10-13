package android.media

import javax.sound.sampled.AudioFormat as JavaAudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

/** Desktop PCM microphone capture backed by javax.sound.sampled. */
class AudioRecord(
    private val audioSource: Int,
    private val sampleRateInHz: Int,
    private val channelConfig: Int,
    private val audioFormat: Int,
    private val bufferSizeInBytes: Int
) {
    companion object {
        const val RECORDSTATE_RECORDING = 3
        const val RECORDSTATE_STOPPED = 1
        const val STATE_INITIALIZED = 1
        const val STATE_UNINITIALIZED = 0
        const val AUDIO_SOURCE_DEFAULT = 0
        const val AUDIO_SOURCE_MIC = 1
        const val AUDIO_SOURCE_VOICE_RECOGNITION = 6
        const val CHANNEL_IN_MONO = 16
        const val CHANNEL_IN_STEREO = 12
        const val ENCODING_PCM_16BIT = 2
        const val ENCODING_PCM_8BIT = 3
        const val ENCODING_PCM_FLOAT = 4
        const val ERROR = -1
        const val ERROR_INVALID_OPERATION = -3
        const val ERROR_BAD_VALUE = -2
        const val SUCCESS = 0

        fun getMinBufferSize(sampleRateInHz: Int, channelConfig: Int, audioFormat: Int): Int {
            val channels = if (channelConfig == CHANNEL_IN_STEREO) 2 else 1
            val bytesPerSample = when (audioFormat) {
                ENCODING_PCM_8BIT -> 1
                ENCODING_PCM_FLOAT -> 4
                else -> 2
            }
            return sampleRateInHz * channels * bytesPerSample / 5
        }
    }

    private var line: TargetDataLine? = null
    private var recording = false

    private fun buildFormat(): JavaAudioFormat {
        val channels = if (channelConfig == CHANNEL_IN_STEREO) 2 else 1
        val sampleSizeBits = when (audioFormat) {
            ENCODING_PCM_8BIT -> 8
            ENCODING_PCM_FLOAT -> 32
            else -> 16
        }
        return JavaAudioFormat(sampleRateInHz.toFloat(), sampleSizeBits, channels, true, false)
    }

    fun getState(): Int = STATE_INITIALIZED

    fun getRecordingState(): Int = if (recording) RECORDSTATE_RECORDING else RECORDSTATE_STOPPED

    fun startRecording() {
        try {
            val format = buildFormat()
            val info = DataLine.Info(TargetDataLine::class.java, format)
            val target = AudioSystem.getLine(info) as TargetDataLine
            target.open(format)
            target.start()
            line = target
            recording = true
        } catch (e: Exception) {
            android.util.Log.e("AudioRecord", "microphone unavailable", e)
            recording = false
        }
    }

    fun read(audioData: ByteArray, offsetInBytes: Int, sizeInBytes: Int): Int {
        val target = line ?: return ERROR_INVALID_OPERATION
        val read = target.read(audioData, offsetInBytes, sizeInBytes)
        return if (read < 0) ERROR else read
    }

    fun read(audioData: ShortArray, offsetInShorts: Int, sizeInShorts: Int): Int {
        val bytes = ByteArray(sizeInShorts * 2)
        val read = read(bytes, 0, bytes.size)
        if (read <= 0) return read
        var i = 0
        while (i + 1 < read) {
            audioData[offsetInShorts + i / 2] = ((bytes[i + 1].toInt() shl 8) or (bytes[i].toInt() and 0xFF)).toShort()
            i += 2
        }
        return read / 2
    }

    fun read(audioData: FloatArray, offsetInFloats: Int, sizeInFloats: Int, readMode: Int): Int {
        val bytes = ByteArray(sizeInFloats * 4)
        val read = read(bytes, 0, bytes.size)
        if (read <= 0) return read
        return read / 4
    }

    fun stop() {
        try {
            line?.stop()
            line?.close()
        } catch (_: Exception) {
        }
        line = null
        recording = false
    }

    fun release() = stop()
}

/** Desktop recorder writing 16-bit PCM WAV output. */
class MediaRecorder {
    companion object {
        const val AudioSource = 0
        const val AudioEncoder = 0
        const val OutputFormat = 0
        const val AudioSource_MIC = 1
        const val AudioSource_VOICE_RECOGNITION = 6
        const val AudioSource_VOICE_COMMUNICATION = 7
        const val AudioSource_CAMCORDER = 5
        const val AudioSource_UNPROCESSED = 9
        const val AudioEncoder_AAC = 3
        const val AudioEncoder_AMR_NB = 1
        const val OutputFormat_MPEG_4 = 2
        const val OutputFormat_THREE_GPP = 1
        const val OutputFormat_AAC_ADTS = 6
        const val OutputFormat_DEFAULT = 0
        const val MEDIA_RECORDER_INFO_MAX_DURATION_REACHED = 800
        const val MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED = 801
    }

    object AudioSource {
        const val DEFAULT = 0
        const val MIC = 1
        const val VOICE_UPLINK = 2
        const val VOICE_DOWNLINK = 3
        const val VOICE_CALL = 4
        const val CAMCORDER = 5
        const val VOICE_RECOGNITION = 6
        const val VOICE_COMMUNICATION = 7
        const val UNPROCESSED = 9
    }

    object AudioEncoder {
        const val DEFAULT = 0
        const val AMR_NB = 1
        const val AMR_WB = 2
        const val AAC = 3
        const val HE_AAC = 4
        const val AAC_ELD = 5
        const val VORBIS = 6
        const val OPUS = 7
    }

    object OutputFormat {
        const val DEFAULT = 0
        const val THREE_GPP = 1
        const val MPEG_4 = 2
        const val AMR_NB = 3
        const val AMR_WB = 4
        const val AAC_ADTS = 6
        const val MPEG_2_TS = 8
        const val WEBM = 9
        const val OGG = 11
    }

    var infoListener: ((MediaRecorder, Int, Int) -> Unit)? = null

    private var outputFile: java.io.File? = null
    private var recording = false
    private var thread: Thread? = null
    private var line: TargetDataLine? = null
    private val chunks = java.io.ByteArrayOutputStream()

    fun setAudioSource(source: Int) {}
    fun setOutputFormat(format: Int) {}
    fun setAudioEncoder(encoder: Int) {}
    fun setAudioSamplingRate(rate: Int) {}
    fun setAudioChannels(channels: Int) {}
    fun setAudioEncodingBitRate(bitRate: Int) {}
    fun setMaxDuration(ms: Int) {}
    fun setOnInfoListener(listener: (MediaRecorder, Int, Int) -> Unit) { infoListener = listener }

    fun setOutputFile(path: String) { outputFile = java.io.File(path) }
    fun setOutputFile(fd: java.io.FileDescriptor) {}
    fun setOutputFile(path: java.io.File) { outputFile = path }

    fun prepare() {}

    fun start() {
        val format = JavaAudioFormat(16000f, 16, 1, true, false)
        val info = DataLine.Info(TargetDataLine::class.java, format)
        val target = AudioSystem.getLine(info) as TargetDataLine
        target.open(format)
        target.start()
        line = target
        recording = true
        chunks.reset()
        thread = Thread {
            val buf = ByteArray(4096)
            while (recording) {
                val read = target.read(buf, 0, buf.size)
                if (read > 0) chunks.write(buf, 0, read)
            }
        }.also { it.isDaemon = true; it.start() }
    }

    fun stop() {
        recording = false
        try {
            line?.stop()
            line?.close()
        } catch (_: Exception) {
        }
        thread?.join(1500)
        val file = outputFile
        if (file != null) {
            writeWav(file, chunks.toByteArray(), 16000, 1)
        }
    }

    fun reset() { chunks.reset() }
    fun release() {
        if (recording) stop()
        line = null
    }

    private fun writeWav(file: java.io.File, pcm: ByteArray, sampleRate: Int, channels: Int) {
        try {
            file.parentFile?.mkdirs()
            java.io.FileOutputStream(file).use { out ->
                val dataLen = pcm.size
                val byteRate = sampleRate * channels * 2
                val header = java.nio.ByteBuffer.allocate(44).order(java.nio.ByteOrder.LITTLE_ENDIAN)
                header.put("RIFF".toByteArray(Charsets.US_ASCII))
                header.putInt(36 + dataLen)
                header.put("WAVE".toByteArray(Charsets.US_ASCII))
                header.put("fmt ".toByteArray(Charsets.US_ASCII))
                header.putInt(16)
                header.putShort(1)
                header.putShort(channels.toShort())
                header.putInt(sampleRate)
                header.putInt(byteRate)
                header.putShort((channels * 2).toShort())
                header.putShort(16)
                header.put("data".toByteArray(Charsets.US_ASCII))
                header.putInt(dataLen)
                out.write(header.array())
                out.write(pcm)
            }
        } catch (e: Exception) {
            android.util.Log.e("MediaRecorder", "failed to write " + file, e)
        }
    }
}

class AudioFormat {
    companion object {
        const val ENCODING_PCM_16BIT = 2
        const val ENCODING_PCM_8BIT = 3
        const val ENCODING_PCM_FLOAT = 4
        const val CHANNEL_IN_MONO = 16
        const val CHANNEL_IN_STEREO = 12
    }
}

class AudioAttributes {
    companion object {
        const val USAGE_MEDIA = 1
        const val USAGE_VOICE_COMMUNICATION = 2
        const val CONTENT_TYPE_SPEECH = 1
        const val CONTENT_TYPE_MUSIC = 2
        const val FLAG_AUDIBILITY_ENFORCED = 1
        const val USAGE_ASSISTANCE_SONIFICATION = 13
        const val USAGE_ASSISTANCE_ACCESSIBILITY = 11
        const val USAGE_ASSISTANCE_NAVIGATION_GUIDANCE = 12
        const val USAGE_NOTIFICATION = 5
        const val USAGE_NOTIFICATION_EVENT = 10
        const val CONTENT_TYPE_SONIFICATION = 4
        const val CONTENT_TYPE_MOVIE = 3
    }

    class Builder {
        fun setUsage(usage: Int): Builder = this
        fun setContentType(type: Int): Builder = this
        fun setFlags(flags: Int): Builder = this
        fun build(): AudioAttributes = AudioAttributes()
    }
}

class AudioFocusRequest {
    class Builder(attrs: AudioAttributes) {
        fun setWillPauseWhenDucked(pause: Boolean): Builder = this
        fun setAcceptsDelayedFocusGain(accept: Boolean): Builder = this
        fun setAudioAttributes(attrs: AudioAttributes): Builder = this
        fun setOnAudioFocusChangeListener(listener: Any?): Builder = this
        fun setOnAudioFocusChangeListener(listener: Any?, handler: android.os.Handler?): Builder = this
        fun build(): AudioFocusRequest = AudioFocusRequest()
    }
}

class AudioManager {
    companion object {
        const val AUDIOFOCUS_GAIN = 1
        const val AUDIOFOCUS_GAIN_TRANSIENT = 2
        const val AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3
        const val AUDIOFOCUS_LOSS = -1
        const val AUDIOFOCUS_REQUEST_GRANTED = 1
        const val AUDIOFOCUS_REQUEST_FAILED = 0
        const val STREAM_MUSIC = 3
        const val MODE_NORMAL = 0
        const val MODE_IN_COMMUNICATION = 3
    }

    fun requestAudioFocus(listener: Any?, streamType: Int, durationHint: Int): Int = AUDIOFOCUS_REQUEST_GRANTED
    fun requestAudioFocus(request: AudioFocusRequest): Int = AUDIOFOCUS_REQUEST_GRANTED
    fun abandonAudioFocus(listener: Any?): Int = AUDIOFOCUS_REQUEST_GRANTED
    fun abandonAudioFocusRequest(request: AudioFocusRequest): Int = AUDIOFOCUS_REQUEST_GRANTED
    fun setMode(mode: Int): Boolean = true
    fun getMode(): Int = MODE_NORMAL
    fun setSpeakerphoneOn(on: Boolean) {}
    fun isWiredHeadsetOn(): Boolean = false
    fun getStreamVolume(streamType: Int): Int = 50
    fun getStreamMaxVolume(streamType: Int): Int = 100
    fun adjustStreamVolume(streamType: Int, direction: Int, flags: Int) {}
    fun startBluetoothSco() {}
    fun stopBluetoothSco() {}
    fun isBluetoothScoOn(): Boolean = false
}

class SoundPool {
    class Builder {
        fun setMaxStreams(maxStreams: Int): Builder = this
        fun setAudioAttributes(attrs: AudioAttributes): Builder = this
        fun build(): SoundPool = SoundPool()
    }

    fun load(path: String, priority: Int): Int = 0
    fun load(context: android.content.Context, resId: Int, priority: Int): Int = 0
    fun play(soundID: Int, leftVolume: Float, rightVolume: Float, priority: Int, loop: Int, rate: Float): Int = 0
    fun stop(streamID: Int) {}
    fun release() {}
    fun setOnLoadCompleteListener(listener: Any?) {}
}

class MediaMetadataRetriever : AutoCloseable {
    companion object {
        const val METADATA_KEY_DURATION = 9
        const val METADATA_KEY_MIMETYPE = 12
        const val METADATA_KEY_VIDEO_WIDTH = 18
        const val METADATA_KEY_VIDEO_HEIGHT = 19
        const val METADATA_KEY_VIDEO_ROTATION = 24
        const val METADATA_KEY_VIDEO_FRAME_COUNT = 32
        const val METADATA_KEY_BITRATE = 20
        const val METADATA_KEY_HAS_VIDEO = 16
        const val METADATA_KEY_HAS_AUDIO = 13

        const val OPTION_PREVIOUS_SYNC = 0
        const val OPTION_NEXT_SYNC = 1
        const val OPTION_CLOSEST_SYNC = 2
        const val OPTION_CLOSEST = 3
    }

    fun setDataSource(path: String) {}
    fun setDataSource(context: android.content.Context, uri: android.net.Uri) {}
    fun setDataSource(fileDescriptor: java.io.FileDescriptor) {}
    fun extractMetadata(code: Int): String? = null

    /** Video frame extraction is not available without a decoder; callers fall back to placeholders. */
    fun getFrameAtTime(): android.graphics.Bitmap? = null
    fun getFrameAtTime(timeUs: Long): android.graphics.Bitmap? = null
    fun getFrameAtTime(timeUs: Long, option: Int): android.graphics.Bitmap? = null
    fun getScaledFrameAtTime(
        timeUs: Long,
        option: Int,
        dstWidth: Int,
        dstHeight: Int
    ): android.graphics.Bitmap? = null
    fun getFrameAtIndex(frameIndex: Int): android.graphics.Bitmap? = null
    fun getScaledFrameAtIndex(
        frameIndex: Int,
        dstWidth: Int,
        dstHeight: Int
    ): android.graphics.Bitmap? = null
    fun release() {}
    override fun close() = release()
}

class AudioRecordNotSupportedException(message: String) : RuntimeException(message)