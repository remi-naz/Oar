package dev.ridill.oar.core.domain.util

import android.app.PendingIntent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object UtilConstants {
    const val DB_MONTH_AND_YEAR_FORMAT = "%m-%Y"

    val DebounceTimeoutDuration: Duration
        get() = 250L.milliseconds

    const val DEFAULT_PAGE_SIZE = 10
    const val DEFAULT_TAG_LIST_LIMIT = 10
    val FieldAutoFocusDelayDuration: Duration
        get() = 300L.milliseconds

    val pendingIntentFlags: Int
        get() = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
}