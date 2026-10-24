package dev.ridill.oar.settings.presentation.settings

import dev.ridill.oar.settings.domain.modal.AppTheme

interface SettingsActions {
    fun onAppThemePreferenceClick()
    fun onAppThemeSelectionDismiss()
    fun onAppThemeSelectionConfirm(appTheme: AppTheme)
    fun onDynamicThemeEnabledChange(enabled: Boolean)
    fun onToggleAutoAddTransactions(enabled: Boolean)
    fun onAutoDetectTxFeatureInfoDismiss()
    fun onAutoDetectTxFeatureInfoAcknowledge()
    fun onSmsPermissionRationaleDismiss()
    fun onSmsPermissionRationaleSettingsClick()
    fun onLoginClick()
    fun onLogoutClick()
    fun onExportLogsClick()
}