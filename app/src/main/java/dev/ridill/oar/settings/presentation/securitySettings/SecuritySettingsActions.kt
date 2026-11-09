package dev.ridill.oar.settings.presentation.securitySettings

import dev.ridill.oar.settings.domain.appLock.AppAutoLockInterval

interface SecuritySettingsActions {
    fun onAppLockToggle(enabled: Boolean)
    fun onAutoLockIntervalSelect(interval: AppAutoLockInterval)
    fun onScreenSecurityToggle(enabled: Boolean)
    fun onExactAlarmPermissionClick()
}
