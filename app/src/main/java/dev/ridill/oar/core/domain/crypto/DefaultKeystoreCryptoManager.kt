package dev.ridill.oar.core.domain.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DefaultKeystoreCryptoManager : KeystoreCryptoManager {

    private val keyStore: KeyStore = KeyStore.getInstance(KeystoreCryptoManager.ANDROID_KEYSTORE)
        .apply { load(null) }

    private fun getOrCreateKey(alias: String): SecretKey {
        keyStore.getKey(alias, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KeystoreCryptoManager.ANDROID_KEYSTORE
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    override fun encrypt(rawData: ByteArray, alias: String): EncryptionResult {
        val cipher = Cipher.getInstance(KeystoreCryptoManager.TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey(alias))
        }
        val encryptedData = cipher.doFinal(rawData)
        return EncryptionResult(
            data = encryptedData,
            iv = cipher.iv
        )
    }

    override fun decrypt(encryptedData: ByteArray, iv: ByteArray, alias: String): ByteArray {
        val cipher = Cipher.getInstance(KeystoreCryptoManager.TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(alias),
                GCMParameterSpec(KeystoreCryptoManager.GCM_TAG_LENGTH, iv)
            )
        }
        return cipher.doFinal(encryptedData)
    }
}
