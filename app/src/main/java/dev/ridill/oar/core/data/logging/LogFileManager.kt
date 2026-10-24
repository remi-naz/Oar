package dev.ridill.oar.core.data.logging

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dev.ridill.oar.core.domain.util.BuildUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Owns the on-device log files.
 *
 * Lines are queued in memory and appended by a single background writer, so logging never blocks the caller.
 * Files live in [Context.getNoBackupFilesDir] (excluded from Auto Backup) and rotate once they reach
 * [MAX_FILE_SIZE_BYTES], keeping at most [MAX_FILE_COUNT] files.
 */
class LogFileManager(
    private val context: Context,
    applicationScope: CoroutineScope
) {
    private val logDir = File(context.noBackupFilesDir, LOG_DIR)
    private val pendingLines = Channel<String>(
        capacity = BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val fileMutex = Mutex()

    init {
        applicationScope.launch(Dispatchers.IO.limitedParallelism(1)) {
            for (line in pendingLines) {
                fileMutex.withLock {
                    writeLines(listOf(line) + drainPendingLines())
                }
            }
        }
    }

    fun append(line: String) {
        pendingLines.trySend(line)
    }

    /**
     * Writes all queued lines synchronously.
     * Meant for the uncaught exception handler, where the process dies right after.
     */
    fun flushBlocking() = runBlocking {
        fileMutex.withLock {
            writeLines(drainPendingLines())
        }
    }

    /**
     * Zips the log files along with non-identifying device info and returns a shareable content [Uri].
     */
    suspend fun exportLogs(): Uri = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            writeLines(drainPendingLines())

            val exportDir = File(context.cacheDir, EXPORT_DIR).apply {
                deleteRecursively()
                mkdirs()
            }
            val timestamp = LocalDateTime.now().format(EXPORT_FILE_TIMESTAMP_FORMATTER)
            val zipFile = File(exportDir, "oar-logs-$timestamp.zip")

            ZipOutputStream(zipFile.outputStream().buffered()).use { zip ->
                zip.putNextEntry(ZipEntry(DEVICE_INFO_FILE_NAME))
                zip.write(buildDeviceInfo().toByteArray())
                zip.closeEntry()

                logFilesOldestFirst().forEach { file ->
                    zip.putNextEntry(ZipEntry(file.name))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}$FILE_PROVIDER_SUFFIX",
                zipFile
            )
        }
    }

    private fun drainPendingLines(): List<String> = buildList {
        while (true) add(pendingLines.tryReceive().getOrNull() ?: break)
    }

    private fun writeLines(lines: List<String>) {
        if (lines.isEmpty()) return
        try {
            logDir.mkdirs()
            val currentFile = logFile(0)
            if (currentFile.length() >= MAX_FILE_SIZE_BYTES) rotateFiles()

            FileOutputStream(currentFile, true).bufferedWriter().use { writer ->
                lines.forEach { line ->
                    writer.write(line)
                    writer.newLine()
                }
            }
        } catch (_: Exception) {
            // Logging must never crash the app, and logging this failure would recurse
        }
    }

    private fun rotateFiles() {
        logFile(MAX_FILE_COUNT - 1).delete()
        for (index in MAX_FILE_COUNT - 2 downTo 0) {
            val file = logFile(index)
            if (file.exists()) file.renameTo(logFile(index + 1))
        }
    }

    private fun logFile(index: Int): File =
        File(logDir, if (index == 0) "$LOG_FILE_NAME.log" else "$LOG_FILE_NAME.$index.log")

    private fun logFilesOldestFirst(): List<File> = (MAX_FILE_COUNT - 1 downTo 0)
        .map(::logFile)
        .filter(File::exists)

    private fun buildDeviceInfo(): String = buildString {
        appendLine("App version: ${BuildUtil.versionName} (${BuildUtil.versionCode})")
        appendLine("Flavor: ${BuildUtil.flavor}, build type: ${BuildUtil.buildType}")
        appendLine("Android: ${BuildUtil.androidVersion} (API ${BuildUtil.apiLevel})")
        appendLine("Device: ${BuildUtil.deviceManufacturer} ${BuildUtil.deviceModel}")
        appendLine("Exported at: ${LocalDateTime.now()}")
    }
}

private const val LOG_DIR = "logs"
private const val LOG_FILE_NAME = "oar"
private const val EXPORT_DIR = "log_exports"
private const val DEVICE_INFO_FILE_NAME = "device-info.txt"
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"
private const val BUFFER_CAPACITY = 1_000
private const val MAX_FILE_SIZE_BYTES = 1_024L * 1_024L
private const val MAX_FILE_COUNT = 3
private val EXPORT_FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
