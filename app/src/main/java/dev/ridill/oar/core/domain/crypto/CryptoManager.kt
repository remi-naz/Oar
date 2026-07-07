package dev.ridill.oar.core.domain.crypto

import android.security.keystore.KeyProperties
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

typealias HashString = String
typealias HashSaltString = String

interface CryptoManager {
    @Throws(IllegalBlockSizeException::class, BadPaddingException::class)
    fun encrypt(rawData: ByteArray, password: String, salt: String): EncryptionResult

    @Throws(IllegalBlockSizeException::class, BadPaddingException::class)
    fun decrypt(encryptedData: ByteArray, iv: ByteArray, password: String, salt: String): ByteArray

    fun generateSalt(): HashSaltString
    fun saltedHash(message: String, salt: String = generateSalt()): Pair<HashString, HashSaltString>
    fun areHashesMatch(value: String?, hash2: String?): Boolean

    companion object {
        const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        const val ITERATION_COUNT = 65536
        const val KEY_LENGTH = 128
        const val KEY_ALGORITHM = "PBKDF2WithHmacSha256"
        const val HASH_LOG_ROUNDS = 15
    }
}

data class EncryptionResult(
    val data: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionResult

        if (!data.contentEquals(other.data)) return false
        return iv.contentEquals(other.iv)
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}

interface KeystoreCryptoManager {
    fun encrypt(rawData: ByteArray): EncryptionResult
    fun decrypt(encryptedData: ByteArray, iv: ByteArray): ByteArray

    companion object {
        const val KEY_ALIAS = "oar_access_token_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH = 128
    }
}