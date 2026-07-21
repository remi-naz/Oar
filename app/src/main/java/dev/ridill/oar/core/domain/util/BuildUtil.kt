package dev.ridill.oar.core.domain.util

import android.os.Build
import dev.ridill.oar.BuildConfig

object BuildUtil {
    val versionName: String get() = BuildConfig.VERSION_NAME

    val isDebug: Boolean get() = BuildConfig.DEBUG

    val isInternal: Boolean get() = BuildConfig.FLAVOR == "internal"

    val isApiLevelAtLeast30: Boolean
        get() = Build.VERSION.SDK_INT >= 30

    val isApiLevelAtLeast31: Boolean
        get() = Build.VERSION.SDK_INT >= 31

    val isApiLevelAtLeastTiramisu: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun isDynamicColorsSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isNotificationRuntimePermissionNeeded(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun isScheduleAlarmRuntimePermissionRequired(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}