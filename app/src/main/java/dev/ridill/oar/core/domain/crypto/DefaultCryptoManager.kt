package dev.ridill.oar.core.domain.crypto

import android.security.keystore.KeyProperties
import org.mindrot.jbcrypt.BCrypt
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Pre-migration BCrypt -> PBKDF2 -> AES/CBC scheme. Decrypt/verify-only: never used to encrypt
 * or hash new data, only to read data produced before the Argon2id/AES-GCM migration.
 */
@Deprecated(message = "Use the new Argon2id/AES-GCM scheme instead")
internal class DefaultCryptoManager : PasswordBasedCryptoManager {

    private companion object {
        const val TRANSFORMATION =
            "${PasswordBasedCryptoManager.ALGORITHM}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
        const val ITERATION_COUNT = 65536
        const val KEY_LENGTH = 128
        const val KEY_ALGORITHM = "PBKDF2WithHmacSha256"
    }

    private fun getDecryptCipher(password: String, salt: String, iv: ByteArray): Cipher = Cipher
        .getInstance(TRANSFORMATION)
        .apply {
            init(Cipher.DECRYPT_MODE, createKey(password, salt), IvParameterSpec(iv))
        }

    private fun createKey(password: String, salt: String): SecretKey {
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val keySpec = PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(),
            ITERATION_COUNT,
            KEY_LENGTH
        )
        val key = factory.generateSecret(keySpec)
        return SecretKeySpec(key.encoded, PasswordBasedCryptoManager.ALGORITHM)
    }

    override fun encrypt(rawData: ByteArray, password: String, salt: String): EncryptionResult {
        throw RuntimeException("Version deprecated")
    }

    override fun decrypt(
        encryptedData: ByteArray,
        iv: ByteArray,
        password: String,
        salt: String
    ): ByteArray = getDecryptCipher(
        password = password,
        salt = salt,
        iv = iv
    ).doFinal(encryptedData)

    override fun generateSalt(): HashSalt = BCrypt.gensalt(15)

    override fun hash(message: String, salt: String): SaltedHash {
        val hash = BCrypt.hashpw(message, salt)
        return hash to salt
    }

    override fun areHashesMatch(value: String?, hash2: String?): Boolean =
        BCrypt.checkpw(value, hash2)
}