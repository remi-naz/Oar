package dev.ridill.oar.core.domain.util

import timber.log.Timber

inline fun logI(tag: String? = null, crossinline message: () -> String) {
    Timber.tag(tag ?: "AppDebug").i(message())
}

inline fun logE(
    throwable: Throwable,
    tag: String? = null,
    crossinline message: () -> String = { String.Empty }
) {
    Timber.tag(tag ?: "AppDebug").e(throwable, message())
}

inline fun logD(tag: String? = null, crossinline message: () -> String) {
    Timber.tag(tag ?: "AppDebug").d(message())
}

/**
 * For failures worth a line in the log file but not a stack trace.
 * Prefer this with the exception class name over logE when a throwable's message could hold personal data.
 */
inline fun logW(tag: String? = null, crossinline message: () -> String) {
    Timber.tag(tag ?: "AppDebug").w(message())
}
