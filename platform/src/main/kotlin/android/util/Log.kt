package android.util

import org.slf4j.LoggerFactory

object Log {
    private val logger = LoggerFactory.getLogger("RikkaHub")
    private fun tag(t: String) = if (t.isBlank()) "" else "[$t] "
    fun v(tag: String, msg: String): Int { logger.trace("{}{}", tag(tag), msg); return 0 }
    fun v(tag: String, msg: String, tr: Throwable?): Int { logger.trace(tag(tag), msg, tr); return 0 }
    fun d(tag: String, msg: String): Int { logger.debug("{}{}", tag(tag), msg); return 0 }
    fun d(tag: String, msg: String, tr: Throwable?): Int { logger.debug(tag(tag), msg, tr); return 0 }
    fun i(tag: String, msg: String): Int { logger.info("{}{}", tag(tag), msg); return 0 }
    fun i(tag: String, msg: String, tr: Throwable?): Int { logger.info(tag(tag), msg, tr); return 0 }
    fun w(tag: String, msg: String): Int { logger.warn("{}{}", tag(tag), msg); return 0 }
    fun w(tag: String, msg: String, tr: Throwable?): Int { logger.warn(tag(tag), msg, tr); return 0 }
    fun w(tag: String, tr: Throwable?): Int { logger.warn(tag(tag), tr); return 0 }
    fun e(tag: String, msg: String): Int { logger.error("{}{}", tag(tag), msg); return 0 }
    fun e(tag: String, msg: String, tr: Throwable?): Int { logger.error(tag(tag), msg, tr); return 0 }
    fun wtf(tag: String, msg: String): Int { logger.error("{}{}", tag(tag), msg); return 0 }
    fun getStackTraceString(tr: Throwable?): String = tr?.stackTraceToString() ?: ""
    fun isLoggable(tag: String, level: Int): Boolean = true
}
