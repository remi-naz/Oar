package dev.ridill.oar.core.data.db

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Matches SQLite's DATETIME() output (space-separated, UTC, whole-second) for a domain
 * LocalDateTime (system-zone local time). Keyset cursors must format cursor values with this
 * so they compare correctly against DATETIME(column) in a raw paged query - using the local-time
 * string directly would shift the cursor boundary by the user's UTC offset.
 */
private val SQLITE_UTC_DATETIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC)

fun LocalDateTime.toSqliteUtcDateTimeString(): String =
    SQLITE_UTC_DATETIME_FORMATTER.format(this.atZone(ZoneId.systemDefault()).toInstant())
