package dev.ridill.oar.account.domain.service

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.ridill.oar.core.domain.crypto.KeystoreCryptoManager
import dev.ridill.oar.core.domain.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AccessTokenKeystoreService(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: KeystoreCryptoManager
) : AccessTokenService {

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val preferences = dataStore.data.first()
        val encodedData = preferences[Keys.ENC_ACCESS_TOKEN] ?: return@withContext null
        val encodedIv = preferences[Keys.ENC_ACCESS_TOKEN_IV] ?: return@withContext null

        try {
            val data = Base64.decode(encodedData, Base64.NO_WRAP)
            val iv = Base64.decode(encodedIv, Base64.NO_WRAP)
            String(cryptoManager.decrypt(data, iv))
        } catch (e: Exception) {
            logE(e) { "Failed to decrypt access token" }
            null
        }
    }

    override suspend fun updateAccessToken(token: String?) = withContext(Dispatchers.IO) {
        if (token == null) {
            dataStore.edit { preferences ->
                preferences.remove(Keys.ENC_ACCESS_TOKEN)
                preferences.remove(Keys.ENC_ACCESS_TOKEN_IV)
            }
            return@withContext
        }

        val encryptionResult = cryptoManager.encrypt(token.toByteArray())
        dataStore.edit { preferences ->
            preferences[Keys.ENC_ACCESS_TOKEN] = Base64.encodeToString(encryptionResult.data, Base64.NO_WRAP)
            preferences[Keys.ENC_ACCESS_TOKEN_IV] = Base64.encodeToString(encryptionResult.iv, Base64.NO_WRAP)
        }
    }

    companion object {
        const val NAME = "access_token_preferences"
    }

    private object Keys {
        val ENC_ACCESS_TOKEN = stringPreferencesKey("ENC_ACCESS_TOKEN")
        val ENC_ACCESS_TOKEN_IV = stringPreferencesKey("ENC_ACCESS_TOKEN_IV")
    }
}
