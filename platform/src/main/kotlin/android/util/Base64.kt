package android.util

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.Base64

object Base64 {
    const val DEFAULT = 0
    const val NO_PADDING = 1
    const val NO_WRAP = 2
    const val CRLF = 4
    const val URL_SAFE = 8
    const val NO_CLOSE = 16

    private fun encoder(flags: Int): Base64.Encoder =
        if (flags and URL_SAFE != 0) java.util.Base64.getUrlEncoder() else java.util.Base64.getEncoder()

    private fun decoder(flags: Int): Base64.Decoder =
        if (flags and URL_SAFE != 0) java.util.Base64.getUrlDecoder() else java.util.Base64.getDecoder()

    fun encodeToString(input: ByteArray, flags: Int = DEFAULT): String {
        var s = encoder(flags).encodeToString(input)
        if (flags and NO_PADDING != 0) s = s.trimEnd('=')
        return s
    }

    fun encodeToString(input: ByteArray, offset: Int, len: Int, flags: Int): String =
        encodeToString(input.copyOfRange(offset, offset + len), flags)

    fun encode(input: ByteArray, flags: Int = DEFAULT): ByteArray =
        encodeToString(input, flags).toByteArray(Charsets.US_ASCII)

    fun decode(str: String, flags: Int = DEFAULT): ByteArray {
        val cleaned = str.trim().replace("\n", "").replace("\r", "")
        var s = cleaned
        if (flags and URL_SAFE == 0) {
            s = s.replace('-', '+').replace('_', '/')
        }
        val pad = (4 - s.length % 4) % 4
        return decoder(flags).decode(s + "=".repeat(pad))
    }

    fun decode(input: ByteArray, flags: Int = DEFAULT): ByteArray = decode(String(input, Charsets.US_ASCII), flags)
}

class Base64OutputStream(private val out: OutputStream, private val flags: Int = 0) : OutputStream() {
    private val buffer = ByteArrayOutputStream()
    override fun write(b: Int) { buffer.write(b); if (buffer.size() >= 1024) flushChunk() }
    override fun write(b: ByteArray, off: Int, len: Int) { buffer.write(b, off, len); if (buffer.size() >= 3072) flushChunk() }
    private fun flushChunk() {
        val data = buffer.toByteArray()
        buffer.reset()
        val enc = android.util.Base64.encodeToString(data, if (flags and android.util.Base64.NO_WRAP != 0) android.util.Base64.NO_PADDING else android.util.Base64.NO_WRAP)
        out.write((enc + if (flags and android.util.Base64.NO_WRAP == 0) "\n" else "").toByteArray(Charsets.US_ASCII))
    }
    override fun flush() { flushChunk(); out.flush() }
    override fun close() { flushChunk(); if (flags and android.util.Base64.NO_CLOSE == 0) out.close() }
}