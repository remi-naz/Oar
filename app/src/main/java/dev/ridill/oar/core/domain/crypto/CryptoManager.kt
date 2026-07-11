package dev.ridill.oar.core.domain.crypto

import android.security.keystore.KeyProperties
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

typealias Hash = String
typealias HashSalt = String
typealias SaltedHash = Pair<Hash, HashSalt>

/**
 * ARGON2_GCM is the current scheme, always written for new backups/hashes.
 * LEGACY_BCRYPT_PBKDF2_CBC is decrypt/verify-only, kept so pre-migration local hashes and
 * cloud backups remain readable; it is never written again.
 */
enum class EncryptionScheme {
    LEGACY_BCRYPT_PBKDF2_CBC,
    ARGON2_GCM;

    companion object {
        fun fromTag(tag: String?): EncryptionScheme =
            entries.firstOrNull { it.name == tag } ?: LEGACY_BCRYPT_PBKDF2_CBC
    }
}

interface PasswordBasedCryptoManager {
    @Throws(IllegalBlockSizeException::class, BadPaddingException::class)
    fun encrypt(
        rawData: ByteArray,
        password: String,
        salt: String
    ): EncryptionResult

    @Throws(IllegalBlockSizeException::class, BadPaddingException::class)
    fun decrypt(
        encryptedData: ByteArray,
        iv: ByteArray,
        password: String,
        salt: String
    ): ByteArray

    fun generateSalt(): HashSalt
    fun hash(
        message: String,
        salt: String = generateSalt()
    ): SaltedHash

    fun areHashesMatch(value: String?, hash2: String?): Boolean

    companion object {
        const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
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
    fun encrypt(rawData: ByteArray, alias: String): EncryptionResult
    fun decrypt(encryptedData: ByteArray, iv: ByteArray, alias: String): ByteArray

    companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH = 128
    }
}