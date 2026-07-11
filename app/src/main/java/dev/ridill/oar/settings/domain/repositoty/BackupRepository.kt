package dev.ridill.oar.settings.domain.repositoty

import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.settings.domain.modal.BackupDetails
import java.time.LocalDateTime

interface BackupRepository {
    suspend fun checkForBackup(): Result<BackupDetails, DataError>
    suspend fun performAppDataBackup()
    suspend fun downloadAndCacheBackupData(fileId: String, timestamp: LocalDateTime)
    suspend fun performAppDataRestoreFromCache(
        password: String,
        passwordSalt: String,
        scheme: EncryptionScheme,
        timestamp: LocalDateTime
    )

    suspend fun tryClearLocalCache()
    suspend fun setBackupError(error: FatalBackupError?)
    suspend fun restoreBackupJobs()
}

enum class FatalBackupError { PASSWORD_CORRUPTED, GOOGLE_AUTH_FAILURE, STORAGE_QUOTA_EXCEEDED }