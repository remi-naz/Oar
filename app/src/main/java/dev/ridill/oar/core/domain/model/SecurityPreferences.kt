package dev.ridill.oar.core.domain.model

import dev.ridill.oar.core.domain.crypto.EncryptionScheme

data class SecurityPreferences(
    val backupEncryptionHash: String?,
    val backupEncryptionHashSalt: String?,
    val backupEncryptionScheme: EncryptionScheme
) {
    val hasValidBackupEncryptionPassword: Boolean
        get() = !backupEncryptionHash.isNullOrEmpty()
                && !backupEncryptionHashSalt.isNullOrEmpty()
}