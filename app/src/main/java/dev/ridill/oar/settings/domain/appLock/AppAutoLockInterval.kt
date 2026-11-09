package dev.ridill.oar.settings.domain.appLock

import androidx.annotation.StringRes
import dev.ridill.oar.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class AppAutoLockInterval(
    val duration: Duration,
    @StringRes val labelRes: Int
) {
    IMMEDIATELY(duration = Duration.ZERO, labelRes = R.string.immediately),
    FIVE_SECONDS(duration = 5.seconds, labelRes = R.string.five_seconds),
    THIRTY_SECONDS(duration = 30.seconds, labelRes = R.string.thirty_seconds),
    ONE_MINUTE(duration = 1.minutes, labelRes = R.string.one_minute);

    companion object {
        val Default = ONE_MINUTE

        /**
         * Resolves a stored name, falling back to [Default] for names that no longer exist
         * (e.g. the removed 5 and 10 minute options).
         */
        fun fromName(name: String?): AppAutoLockInterval =
            entries.firstOrNull { it.name == name } ?: Default
    }
}
