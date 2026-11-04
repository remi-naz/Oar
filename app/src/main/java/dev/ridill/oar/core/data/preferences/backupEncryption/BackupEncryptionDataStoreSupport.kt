package dev.ridill.oar.core.data.preferences.backupEncryption

import android.content.Context
import android.util.Base64
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataMigration
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dev.ridill.oar.core.domain.crypto.KeystoreCryptoManager
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.domain.util.logI
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.KeyStore

private const val TAG = "BackupEncryptionDataStore"
private const val KEYSET_NAME = "oar_backup_encryption_keyset"
private const val KEYSET_PREF_FILE_NAME = "oar_backup_encryption_keyset_prefs"
private const val MASTER_KEY_URI = "android-keystore://oar_backup_encryption_master_key"

internal const val BACKUP_ENCRYPTION_STORE_FILE_NAME = BackupEncryptionPreferencesManager.NAME

// File name of the plaintext store used before the Tink-encrypted one
private const val LEGACY_STORE_FILE_NAME = "security_preferences"

internal fun Context.buildBackupEncryptionAead(): Aead {
    AeadConfig.register()
    val keysetHandle = AndroidKeysetManager.Builder()
        .withSharedPref(this, KEYSET_NAME, KEYSET_PREF_FILE_NAME)
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri(MASTER_KEY_URI)
        .build()
        .keysetHandle
    return keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
}

/**
 * Surfaces decryption failures (e.g. lost keyset) as [CorruptionException]
 * so that the store's corruption handler can reset it instead of crashing.
 */
internal class DecryptionCorruptionSerializer<T>(
    private val delegate: Serializer<T>
) : Serializer<T> {
    override val defaultValue: T get() = delegate.defaultValue

    override suspend fun readFrom(input: InputStream): T = try {
        delegate.readFrom(input)
    } catch (e: GeneralSecurityException) {
        throw CorruptionException("Unable to decrypt security preferences", e)
    }

    override suspend fun writeTo(t: T, output: OutputStream) = delegate.writeTo(t, output)
}

/**
 * Moves data from the legacy plaintext security preferences file, where the backup encryption
 * hash was individually wrapped with a Keystore key, into the Tink-encrypted store.
 */
internal class LegacyBackupEncryptionPreferencesMigration(
    private val context: Context,
    private val keystoreCryptoManager: KeystoreCryptoManager
) : DataMigration<Preferences> {
    private val legacyFile: File
        get() = context.preferencesDataStoreFile(LEGACY_STORE_FILE_NAME)

    override suspend fun shouldMigrate(currentData: Preferences): Boolean = legacyFile.exists()

    override suspend fun migrate(currentData: Preferences): Preferences {
        val legacy = try {
            legacyFile.inputStream().use { PreferencesFileSerializer.readFrom(it) }
        } catch (e: Exception) {
            logE(e, TAG) { "Failed to read legacy security preferences" }
            return currentData
        }

        val hash = readLegacyHash(legacy)
        val migrated = currentData.toMutablePreferences()
        hash?.let { migrated[BackupEncryptionPreferencesManagerImpl.Keys.BACKUP_ENCRYPTION_HASH] = it }
        legacy[BackupEncryptionPreferencesManagerImpl.Keys.BACKUP_ENCRYPTION_HASH_SALT]
            ?.let { migrated[BackupEncryptionPreferencesManagerImpl.Keys.BACKUP_ENCRYPTION_HASH_SALT] = it }
        legacy[BackupEncryptionPreferencesManagerImpl.Keys.BACKUP_ENCRYPTION_SCHEME]
            ?.let { migrated[BackupEncryptionPreferencesManagerImpl.Keys.BACKUP_ENCRYPTION_SCHEME] = it }
        logI(TAG) { "Migrated legacy security preferences" }
        return migrated
    }

    private fun readLegacyHash(legacy: Preferences): String? {
        val encodedHash = legacy[LegacyKeys.HASH_ENC]
        val encodedIv = legacy[LegacyKeys.HASH_IV]
        if (encodedHash != null && encodedIv != null) {
            return try {
                val data = Base64.decode(encodedHash, Base64.NO_WRAP)
                val iv = Base64.decode(encodedIv, Base64.NO_WRAP)
                String(keystoreCryptoManager.decrypt(data, iv, LEGACY_HASH_KEY_ALIAS))
            } catch (e: Exception) {
                logE(e, TAG) { "Failed to decrypt legacy backup encryption hash" }
                null
            }
        }
        return legacy[BackupEncryptionPreferencesManagerImpl.Keys.BACKUP_ENCRYPTION_HASH]
    }

    override suspend fun cleanUp() {
        try {
            legacyFile.delete()
            val keyStore = KeyStore.getInstance(KeystoreCryptoManager.ANDROID_KEYSTORE)
                .apply { load(null) }
            if (keyStore.containsAlias(LEGACY_HASH_KEY_ALIAS))
                keyStore.deleteEntry(LEGACY_HASH_KEY_ALIAS)
        } catch (e: Exception) {
            logE(e, TAG) { "Failed to clean up legacy security preferences" }
        }
    }

    private object LegacyKeys {
        val HASH_ENC = stringPreferencesKey("BACKUP_ENCRYPTION_HASH_ENC")
        val HASH_IV = stringPreferencesKey("BACKUP_ENCRYPTION_HASH_IV")
    }

    private companion object {
        const val LEGACY_HASH_KEY_ALIAS = "oar_backup_encryption_key"
    }
}
