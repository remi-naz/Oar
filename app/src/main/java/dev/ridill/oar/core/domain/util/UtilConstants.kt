package dev.ridill.oar.core.domain.util

import android.app.PendingIntent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object UtilConstants {
    const val DB_MONTH_AND_YEAR_FORMAT = "%m-%Y"
    const val DEBOUNCE_TIMEOUT = 250L
    val DebounceTimeoutDuration: Duration
        get() = DEBOUNCE_TIMEOUT.milliseconds

    const val DEFAULT_PAGE_SIZE = 10
    const val DEFAULT_TAG_LIST_LIMIT = 10
    const val FIELD_AUTO_FOCUS_DELAY = 300L
    val FieldAutoFocusDelayDuration: Duration
        get() = FIELD_AUTO_FOCUS_DELAY.milliseconds

    val pendingIntentFlags: Int
        get() = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
}