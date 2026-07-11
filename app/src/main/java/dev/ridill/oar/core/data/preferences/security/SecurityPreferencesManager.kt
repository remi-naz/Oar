package dev.ridill.oar.core.data.preferences.security

import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.model.SecurityPreferences
import kotlinx.coroutines.flow.Flow

interface SecurityPreferencesManager {
    companion object {
        const val NAME = "security_preferences"
    }

    val preferences: Flow<SecurityPreferences>

    suspend fun updateBackupEncryptionHash(hash: String?, salt: String?, scheme: EncryptionScheme)
}