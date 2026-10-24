package dev.ridill.oar.core.data.logging

import android.util.Log
import dev.ridill.oar.core.domain.logging.LogRedactor
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * [Timber.Tree] that persists INFO and above to rotating log files via [LogFileManager].
 * DEBUG and VERBOSE stay logcat-only so detailed debugging output never reaches disk.
 */
class FileLoggingTree(
    private val logFileManager: LogFileManager,
    private val logRedactor: LogRedactor
) : Timber.Tree() {

    private val timestampFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .withZone(ZoneId.systemDefault())

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.INFO

    // Timber has already appended the throwable's stack trace to message, so it gets redacted too
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val line = buildString {
            append(timestampFormatter.format(Instant.now()))
            append(' ')
            append(priorityLabel(priority))
            append('/')
            append(tag ?: "Oar")
            append(": ")
            append(logRedactor.redact(message))
        }
        logFileManager.append(line)
    }

    private fun priorityLabel(priority: Int): Char = when (priority) {
        Log.VERBOSE -> 'V'
        Log.DEBUG -> 'D'
        Log.INFO -> 'I'
        Log.WARN -> 'W'
        Log.ERROR -> 'E'
        else -> 'A'
    }
}
