package dev.ridill.oar.settings.data.repository

import androidx.work.WorkInfo
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.crypto.PasswordBasedCryptoManager
import dev.ridill.oar.di.Argon2PasswordBasedCryptoManager
import dev.ridill.oar.settings.data.local.ConfigDao
import dev.ridill.oar.settings.data.local.ConfigKey
import dev.ridill.oar.settings.data.local.entity.ConfigEntity
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.modal.BackupInterval
import dev.ridill.oar.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class BackupSettingsRepositoryImpl(
    private val dao: ConfigDao,
    private val preferencesManager: PreferencesManager,
    private val securityPreferencesManager: SecurityPreferencesManager,
    private val backupWorkManager: BackupWorkManager,
    @Argon2PasswordBasedCryptoManager private val argon2CryptoManager: PasswordBasedCryptoManager,
    private val legacyCryptoManager: PasswordBasedCryptoManager
) : BackupSettingsRepository {

    override fun getLastBackupTime(): Flow<LocalDateTime?> = preferencesManager.preferences
        .mapLatest { it.lastBackupDateTime }
        .distinctUntilChanged()

    override fun getImmediateBackupWorkInfo(): Flow<WorkInfo?> =
        backupWorkManager.getImmediateBackupWorkInfoFlow()

    override fun getPeriodicBackupWorkInfo(): Flow<WorkInfo?> =
        backupWorkManager.getPeriodicBackupWorkInfoFlow()

    override fun getIntervalFromInfo(workInfo: WorkInfo): BackupInterval? =
        backupWorkManager.getBackupIntervalFromWorkInfo(workInfo)

    override suspend fun updateBackupIntervalAndScheduleJob(interval: BackupInterval) =
        withContext(Dispatchers.IO) {
            val entity = ConfigEntity(
                configKey = ConfigKey.BACKUP_INTERVAL,
                configValue = interval.name
            )
            dao.upsert(entity)

            if (interval == BackupInterval.MANUAL) {
                backupWorkManager.cancelPeriodicBackupWork()
            } else {
                backupWorkManager.schedulePeriodicBackupWork(interval)
            }
        }

    override fun runBackupJob(interval: BackupInterval) {
        if (interval == BackupInterval.MANUAL) {
            backupWorkManager.runImmediateBackupWork()
        } else {
            backupWorkManager.schedulePeriodicBackupWork(interval)
        }
    }

    override fun runImmediateBackupJob() {
        backupWorkManager.runImmediateBackupWork()
    }

    override suspend fun restoreBackupJob() {
        val backupInterval = BackupInterval.valueOf(
            dao.getBackupInterval() ?: BackupInterval.MANUAL.name
        )
        backupWorkManager.schedulePeriodicBackupWork(backupInterval)
    }

    override suspend fun isCurrentPasswordMatch(currentPasswordInput: String): Boolean {
        val securityPreferences = securityPreferencesManager.preferences.first()
        val passwordHash = securityPreferences.backupEncryptionHash
        return when (securityPreferences.backupEncryptionScheme) {
            EncryptionScheme.ARGON2_GCM -> argon2CryptoManager.areHashesMatch(
                value = currentPasswordInput,
                hash2 = passwordHash
            )

            EncryptionScheme.LEGACY_BCRYPT_PBKDF2_CBC -> legacyCryptoManager.areHashesMatch(
                value = currentPasswordInput,
                hash2 = passwordHash
            )
        }
    }

    override suspend fun updateEncryptionPassword(
        password: String
    ): Unit = withContext(Dispatchers.IO) {
        val (hash, salt) = argon2CryptoManager.hash(password)
        securityPreferencesManager.updateBackupEncryptionHash(
            hash = hash,
            salt = salt,
            scheme = EncryptionScheme.ARGON2_GCM
        )
    }

    override fun getFatalBackupError(): Flow<FatalBackupError?> = preferencesManager
        .preferences
        .mapLatest { it.fatalBackupError }
        .distinctUntilChanged()

    override fun isEncryptionPasswordAvailable(): Flow<Boolean> =
        securityPreferencesManager.preferences
            .mapLatest { it.hasValidBackupEncryptionPassword }
            .distinctUntilChanged()
}

