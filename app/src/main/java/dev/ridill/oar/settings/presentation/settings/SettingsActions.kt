package dev.ridill.oar.settings.presentation.settings

import dev.ridill.oar.settings.domain.modal.AppTheme

interface SettingsActions {
    fun onAppThemePreferenceClick()
    fun onAppThemeSelectionDismiss()
    fun onAppThemeSelectionConfirm(appTheme: AppTheme)
    fun onDynamicThemeEnabledChange(enabled: Boolean)
    fun onLoginClick()
    fun onLogoutClick()
    fun onLogoutDialogDismiss()
    fun onLogoutKeepDataClick()
    fun onLogoutClearDataClick()
    fun onLogoutClearDataConfirm()
}