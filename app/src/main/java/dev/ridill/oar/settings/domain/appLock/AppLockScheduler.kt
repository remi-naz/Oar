package dev.ridill.oar.settings.domain.appLock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.tryOrNull
import androidx.core.content.edit

/**
 * Schedules the alarm that locks the app after it has been in the background for the
 * selected [AppAutoLockInterval].
 *
 * Uses an exact alarm when the exact alarm permission is available so short intervals are
 * honoured, and falls back to an inexact (but still idle-safe) alarm otherwise.
 */
class AppLockScheduler(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val deadlinePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    /**
     * Whether the auto lock interval has already run out since [schedule] was called.
     * The alarm can fire late (or be lost, e.g. across a reboot), so callers use this
     * on return to the app instead of relying on the alarm having fired.
     */
    fun hasDeadlinePassed(): Boolean = isDeadlinePassed(
        deadline = deadlinePrefs.getLong(KEY_DEADLINE, NO_DEADLINE).takeIf { it != NO_DEADLINE },
        storedBootCount = deadlinePrefs.getInt(KEY_BOOT_COUNT, UNKNOWN_BOOT_COUNT),
        currentBootCount = currentBootCount(),
        now = SystemClock.elapsedRealtime()
    )

    fun schedule(interval: AppAutoLockInterval) {
        val triggerAtMillis = SystemClock.elapsedRealtime() + interval.duration.inWholeMilliseconds
        deadlinePrefs.edit {
            putLong(KEY_DEADLINE, triggerAtMillis)
                .putInt(KEY_BOOT_COUNT, currentBootCount())
        }
        val pendingIntent = buildPendingIntent()
        // A security timer must never crash the app if the alarm can't be set
        tryOrNull(TAG) {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            logI(TAG) { "App lock alarm scheduled in ${interval.duration}" }
        }
    }

    /** Cancels the alarm and forgets the deadline. */
    fun cancel() {
        alarmManager.cancel(buildPendingIntent())
        deadlinePrefs.edit {
            remove(KEY_DEADLINE)
                .remove(KEY_BOOT_COUNT)
        }
        logI(TAG) { "App lock alarm cancelled" }
    }

    // elapsedRealtime resets on reboot, so the boot count tells us the stored deadline is stale
    private fun currentBootCount(): Int = Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.BOOT_COUNT,
        UNKNOWN_BOOT_COUNT
    )

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, AppLockReceiver::class.java).apply {
            action = AppLockReceiver.ACTION_LOCK_APP
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            UtilConstants.pendingIntentFlags
        )
    }
}

/**
 * A deadline from a previous boot is always treated as passed: the clock it was measured
 * against no longer exists, and the app was certainly in the background past the interval.
 */
internal fun isDeadlinePassed(
    deadline: Long?,
    storedBootCount: Int,
    currentBootCount: Int,
    now: Long
): Boolean = when {
    deadline == null -> false
    storedBootCount != currentBootCount -> true
    else -> now >= deadline
}

private const val TAG = "AppLockScheduler"
private const val REQUEST_CODE = 7001
private const val PREFS_NAME = "app_lock_timer"
private const val KEY_DEADLINE = "deadline_elapsed_realtime"
private const val KEY_BOOT_COUNT = "boot_count"
private const val NO_DEADLINE = -1L
private const val UNKNOWN_BOOT_COUNT = -1
