package dev.ridill.oar.settings.presentation.settings

import dev.ridill.oar.account.domain.model.AuthState
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.settings.domain.modal.AppTheme
import java.time.LocalDateTime

data class SettingsState(
    val authState: AuthState = AuthState.UnAuthenticated,
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val dynamicColorsEnabled: Boolean = false,
    val showAppThemeSelection: Boolean = false,
    val currentMonthlyBudget: Long = Long.Zero,
    val showAutoDetectTxOption: Boolean = false,
    val autoDetectTransactionEnabled: Boolean = false,
    val showSmsPermissionRationale: Boolean = false,
    val showAutoDetectTransactionFeatureInfo: Boolean = false,
    val sourceCodeUrl: String? = null,
    val logoutDialog: LogoutDialog? = null,
    val lastBackupDateTime: LocalDateTime? = null,
    val isClearingLocalData: Boolean = false
) {
    val hasValidSourceCodeUrl: Boolean
        get() = !sourceCodeUrl.isNullOrEmpty()

    val lastBackupDateFormatted: UiText?
        get() = lastBackupDateTime?.let { DateUtil.Formatters.prettyDateAgo(it.toLocalDate()) }
}

enum class LogoutDialog {
    CHOICE,
    CONFIRM_CLEAR_DATA
}
