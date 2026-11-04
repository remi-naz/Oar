package dev.ridill.oar.core.data.preferences.backupEncryption

import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.model.BackupEncryptionPreferences
import kotlinx.coroutines.flow.Flow

interface BackupEncryptionPreferencesManager {
    companion object {
        const val NAME = "backup_encryption_preferences"
    }

    val preferences: Flow<BackupEncryptionPreferences>

    suspend fun updateBackupEncryptionHash(hash: String?, salt: String?, scheme: EncryptionScheme)
}