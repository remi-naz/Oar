package dev.ridill.oar.core.data.preferences.backupEncryption

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.model.BackupEncryptionPreferences
import dev.ridill.oar.core.domain.util.logE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import java.io.IOException

internal class BackupEncryptionPreferencesManagerImpl(
    private val dataStore: DataStore<Preferences>
) : BackupEncryptionPreferencesManager {
    override val preferences: Flow<BackupEncryptionPreferences> = dataStore.data
        .catch { cause ->
            if (cause is IOException) {
                logE(cause) { "Preferences Exception" }
                emit(emptyPreferences())
            } else throw cause
        }
        .mapLatest { preferences ->
            val backupEncryptionHash = preferences[Keys.BACKUP_ENCRYPTION_HASH]
            val backupEncryptionHashSalt = preferences[Keys.BACKUP_ENCRYPTION_HASH_SALT]
            val backupEncryptionScheme =
                EncryptionScheme.fromTag(preferences[Keys.BACKUP_ENCRYPTION_SCHEME])

            BackupEncryptionPreferences(
                backupEncryptionHash = backupEncryptionHash,
                backupEncryptionHashSalt = backupEncryptionHashSalt,
                backupEncryptionScheme = backupEncryptionScheme
            )
        }

    override suspend fun updateBackupEncryptionHash(
        hash: String?,
        salt: String?,
        scheme: EncryptionScheme
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.BACKUP_ENCRYPTION_HASH] = hash.orEmpty()
            preferences[Keys.BACKUP_ENCRYPTION_HASH_SALT] = salt.orEmpty()
            preferences[Keys.BACKUP_ENCRYPTION_SCHEME] = scheme.name
        }
    }

    object Keys {
        val BACKUP_ENCRYPTION_HASH = stringPreferencesKey("BACKUP_ENCRYPTION_HASH")
        val BACKUP_ENCRYPTION_HASH_SALT = stringPreferencesKey("BACKUP_ENCRYPTION_HASH_SALT")
        val BACKUP_ENCRYPTION_SCHEME = stringPreferencesKey("BACKUP_ENCRYPTION_SCHEME")
    }
}