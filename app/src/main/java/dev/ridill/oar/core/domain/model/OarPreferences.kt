package dev.ridill.oar.core.domain.model

import dev.ridill.oar.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import java.time.LocalDateTime

data class OarPreferences(
    val showOnboarding: Boolean,
    val appTheme: AppTheme,
    val dynamicColorsEnabled: Boolean,
    val lastBackupDateTime: LocalDateTime?,
    val transactionAutoDetectEnabled: Boolean,
    val allTransactionsShowExcludedOption: Boolean,
    val appLockEnabled: Boolean,
    val appAutoLockInterval: AppAutoLockInterval,
    val isAppLocked: Boolean,
    val screenSecurityEnabled: Boolean,
    val fatalBackupError: FatalBackupError?,
    val showAutoDetectTxInfo: Boolean,
    val remoteConfigForceRefreshRequired: Boolean,
)