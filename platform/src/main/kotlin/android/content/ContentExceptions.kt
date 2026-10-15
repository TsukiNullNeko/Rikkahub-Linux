package android.content

class ActivityNotFoundException(message: String? = null) : RuntimeException(message)

class ReceiverCallNotAllowedException(message: String? = null) : RuntimeException(message)

class OperationCanceledException(message: String? = null) : RuntimeException(message)
