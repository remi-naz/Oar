package dev.ridill.oar.fakes

import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.data.preferences.backupEncryption.BackupEncryptionPreferencesManager
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.model.BackupEncryptionPreferences
import dev.ridill.oar.core.domain.model.OarPreferences
import dev.ridill.oar.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.LocalDateTime

/**
 * Only [resetDataPreferences] is observable; the other writers are accepted and ignored.
 * [onReset] runs as the reset happens, so a test can capture what state the system was in then.
 */
class FakePreferencesManager(
    private val onReset: () -> Unit = {}
) : PreferencesManager {

    var resetCount = 0
        private set

    override val preferences: Flow<OarPreferences> = emptyFlow()

    override suspend fun concludeOnboarding() = Unit
    override suspend fun updateAppThem(theme: AppTheme) = Unit
    override suspend fun updateDynamicColorsEnabled(enabled: Boolean) = Unit
    override suspend fun updateLastBackupTimestamp(localDateTime: LocalDateTime) = Unit
    override suspend fun updateTransactionAutoDetectEnabled(enabled: Boolean) = Unit
    override suspend fun updateAllTransactionsShowExcludedOption(show: Boolean) = Unit
    override suspend fun updateAppLockEnabled(enabled: Boolean) = Unit
    override suspend fun updateAppAutoLockInterval(interval: AppAutoLockInterval) = Unit
    override suspend fun updateAppLocked(locked: Boolean) = Unit
    override suspend fun updateScreenSecurityEnabled(enabled: Boolean) = Unit
    override suspend fun updateFatalBackupError(error: FatalBackupError?) = Unit
    override suspend fun toggleShowAutoDetectTxInfoFalse() = Unit
    override suspend fun updateRemoteConfigForceRefreshRequired(required: Boolean) = Unit

    override suspend fun resetDataPreferences() {
        resetCount++
        onReset()
    }
}

class FakeBackupEncryptionPreferencesManager : BackupEncryptionPreferencesManager {

    data class HashUpdate(val hash: String?, val salt: String?, val scheme: EncryptionScheme)

    val hashUpdates = mutableListOf<HashUpdate>()

    override val preferences: Flow<BackupEncryptionPreferences> = emptyFlow()

    override suspend fun updateBackupEncryptionHash(
        hash: String?,
        salt: String?,
        scheme: EncryptionScheme
    ) {
        hashUpdates += HashUpdate(hash, salt, scheme)
    }
}
