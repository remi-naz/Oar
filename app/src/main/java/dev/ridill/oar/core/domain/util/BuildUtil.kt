package dev.ridill.oar.core.domain.util

import android.os.Build
import dev.ridill.oar.BuildConfig

object BuildUtil {
    val versionName: String get() = BuildConfig.VERSION_NAME

    val versionCode: Int get() = BuildConfig.VERSION_CODE

    val flavor: String get() = BuildConfig.FLAVOR

    val buildType: String get() = BuildConfig.BUILD_TYPE

    val isDebug: Boolean get() = BuildConfig.DEBUG

    val isInternal: Boolean get() = flavor == "internal"

    val androidVersion: String get() = Build.VERSION.RELEASE

    val apiLevel: Int get() = Build.VERSION.SDK_INT

    val deviceManufacturer: String get() = Build.MANUFACTURER

    val deviceModel: String get() = Build.MODEL

    val isApiLevelAtLeast30: Boolean
        get() = Build.VERSION.SDK_INT >= 30

    val isApiLevelAtLeastTiramisu: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun isDynamicColorsSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isNotificationRuntimePermissionNeeded(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}