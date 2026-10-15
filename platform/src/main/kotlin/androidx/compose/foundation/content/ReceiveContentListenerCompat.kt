package androidx.compose.foundation.content

/**
 * SAM-style factory mirroring the Android Compose API:
 * ReceiveContentListener { transferableContent -> ... }
 *
 * The handler receives the incoming content and returns the (possibly modified) content
 * to keep, or null to consume it entirely.
 */
inline fun ReceiveContentListener(
    crossinline onReceive: (TransferableContent) -> TransferableContent?
): ReceiveContentListener = object : ReceiveContentListener {
    override fun onReceive(transferableContent: TransferableContent): TransferableContent? =
        onReceive(transferableContent)
}
