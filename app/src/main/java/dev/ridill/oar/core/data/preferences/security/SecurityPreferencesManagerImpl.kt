package dev.ridill.oar.core.data.preferences.security

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.crypto.KeystoreCryptoManager
import dev.ridill.oar.core.domain.model.SecurityPreferences
import dev.ridill.oar.core.domain.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.io.IOException

internal class SecurityPreferencesManagerImpl(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: KeystoreCryptoManager
) : SecurityPreferencesManager {
    override val preferences: Flow<SecurityPreferences> = dataStore.data
        .catch { cause ->
            if (cause is IOException) {
                logE(cause) { "Preferences Exception" }
                emit(emptyPreferences())
            } else throw cause
        }
        .mapLatest { preferences ->
            val backupEncryptionHash = readBackupEncryptionHash(preferences)
            val backupEncryptionHashSalt = preferences[Keys.BACKUP_ENCRYPTION_HASH_SALT]
            val backupEncryptionScheme =
                EncryptionScheme.fromTag(preferences[Keys.BACKUP_ENCRYPTION_SCHEME])

            SecurityPreferences(
                backupEncryptionHash = backupEncryptionHash,
                backupEncryptionHashSalt = backupEncryptionHashSalt,
                backupEncryptionScheme = backupEncryptionScheme
            )
        }

    private suspend fun readBackupEncryptionHash(preferences: Preferences): String? {
        val encodedHash = preferences[Keys.BACKUP_ENCRYPTION_HASH_ENC]
        val encodedIv = preferences[Keys.BACKUP_ENCRYPTION_HASH_IV]
        if (encodedHash != null && encodedIv != null) {
            return try {
                val data = Base64.decode(encodedHash, Base64.NO_WRAP)
                val iv = Base64.decode(encodedIv, Base64.NO_WRAP)
                String(cryptoManager.decrypt(data, iv, alias = BACKUP_HASH_KEY_ALIAS))
            } catch (e: Exception) {
                logE(e) { "Failed to decrypt backup encryption hash" }
                null
            }
        }

        // Pre-migration plaintext hash from before Keystore wrapping was introduced.
        // Encrypt and persist it once, then keep serving from the new keys going forward.
        val legacyHash = preferences[Keys.BACKUP_ENCRYPTION_HASH] ?: return null
        if (legacyHash.isNotEmpty()) persistEncryptedHash(legacyHash)
        return legacyHash
    }

    override suspend fun updateBackupEncryptionHash(
        hash: String?,
        salt: String?,
        scheme: EncryptionScheme
    ) {
        withContext(Dispatchers.IO) {
            val encryptionResult = cryptoManager.encrypt(
                rawData = hash.orEmpty().toByteArray(),
                alias = BACKUP_HASH_KEY_ALIAS
            )
            dataStore.edit { preferences ->
                preferences.remove(Keys.BACKUP_ENCRYPTION_HASH)
                preferences[Keys.BACKUP_ENCRYPTION_HASH_ENC] =
                    Base64.encodeToString(encryptionResult.data, Base64.NO_WRAP)
                preferences[Keys.BACKUP_ENCRYPTION_HASH_IV] =
                    Base64.encodeToString(encryptionResult.iv, Base64.NO_WRAP)
                preferences[Keys.BACKUP_ENCRYPTION_HASH_SALT] = salt.orEmpty()
                preferences[Keys.BACKUP_ENCRYPTION_SCHEME] = scheme.name
            }
        }
    }

    private suspend fun persistEncryptedHash(hash: String) = withContext(Dispatchers.IO) {
        val encryptionResult = cryptoManager.encrypt(
            rawData = hash.toByteArray(),
            alias = BACKUP_HASH_KEY_ALIAS
        )
        dataStore.edit { preferences ->
            preferences.remove(Keys.BACKUP_ENCRYPTION_HASH)
            preferences[Keys.BACKUP_ENCRYPTION_HASH_ENC] =
                Base64.encodeToString(encryptionResult.data, Base64.NO_WRAP)
            preferences[Keys.BACKUP_ENCRYPTION_HASH_IV] =
                Base64.encodeToString(encryptionResult.iv, Base64.NO_WRAP)
        }
    }

    private companion object {
        const val BACKUP_HASH_KEY_ALIAS = "oar_backup_encryption_key"
    }

    object Keys {
        val BACKUP_ENCRYPTION_HASH = stringPreferencesKey("BACKUP_ENCRYPTION_HASH")
        val BACKUP_ENCRYPTION_HASH_ENC = stringPreferencesKey("BACKUP_ENCRYPTION_HASH_ENC")
        val BACKUP_ENCRYPTION_HASH_IV = stringPreferencesKey("BACKUP_ENCRYPTION_HASH_IV")
        val BACKUP_ENCRYPTION_HASH_SALT = stringPreferencesKey("BACKUP_ENCRYPTION_HASH_SALT")
        val BACKUP_ENCRYPTION_SCHEME = stringPreferencesKey("BACKUP_ENCRYPTION_SCHEME")
    }
}