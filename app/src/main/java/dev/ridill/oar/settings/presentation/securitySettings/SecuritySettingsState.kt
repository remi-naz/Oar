package dev.ridill.oar.settings.presentation.securitySettings

import dev.ridill.oar.settings.domain.appLock.AppAutoLockInterval

data class SecuritySettingsState(
    val appLockEnabled: Boolean = false,
    val autoLockInterval: AppAutoLockInterval = AppAutoLockInterval.Default,
    val screenSecurityEnabled: Boolean = false,
    val exactAlarmPermissionRequired: Boolean = false
)
