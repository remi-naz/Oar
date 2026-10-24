package dev.ridill.oar.core.data.preferences

import dev.ridill.oar.core.domain.model.OarPreferences
import dev.ridill.oar.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface PreferencesManager {
    companion object {
        const val NAME = "preferences"
    }

    val preferences: Flow<OarPreferences>

    suspend fun concludeOnboarding()
    suspend fun updateAppThem(theme: AppTheme)
    suspend fun updateDynamicColorsEnabled(enabled: Boolean)
    suspend fun updateLastBackupTimestamp(localDateTime: LocalDateTime)
    suspend fun updateTransactionAutoDetectEnabled(enabled: Boolean)
    suspend fun updateAllTransactionsShowExcludedOption(show: Boolean)
    suspend fun updateAppLockEnabled(enabled: Boolean)
    suspend fun updateAppAutoLockInterval(interval: AppAutoLockInterval)
    suspend fun updateAppLocked(locked: Boolean)
    suspend fun updateScreenSecurityEnabled(enabled: Boolean)
    suspend fun updateFatalBackupError(error: FatalBackupError?)
    suspend fun toggleShowAutoDetectTxInfoFalse()

    /**
     * Resets data-derived preferences and re-enables onboarding; app-level choices like theme and
     * app lock are kept
     */
    suspend fun resetDataPreferences()
}