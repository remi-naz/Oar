package dev.ridill.oar.core.domain.crypto

import android.security.keystore.KeyProperties
import android.util.Base64
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Current scheme: Argon2id -> AES-256/GCM.
 */
internal class Argon2CryptoManager : PasswordBasedCryptoManager {

    private val argon2 = Argon2Kt()

    private companion object {
        const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        const val TRANSFORMATION =
            "$ALGORITHM/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
        const val GCM_TAG_LENGTH_BITS = 128
        const val GCM_NONCE_LENGTH_BYTES = 12
        const val TIME_COST = 3
        const val MEMORY_COST_KIB = 65536
        const val PARALLELISM = 4
        const val KEY_LENGTH_BYTES = 32
        const val SALT_LENGTH_BYTES = 16
        val ARGON2_MODE = Argon2Mode.ARGON2_ID
    }

    private fun createKey(password: String, salt: String): SecretKey {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val result = argon2.hash(
            mode = ARGON2_MODE,
            password = password.toByteArray(Charsets.UTF_8),
            salt = saltBytes,
            tCostInIterations = TIME_COST,
            mCostInKibibyte = MEMORY_COST_KIB,
            parallelism = PARALLELISM,
            hashLengthInBytes = KEY_LENGTH_BYTES
        )

        return SecretKeySpec(result.rawHashAsByteArray(), ALGORITHM)
    }

    override fun encrypt(
        rawData: ByteArray,
        password: String,
        salt: String
    ): EncryptionResult {
        val nonce = ByteArray(GCM_NONCE_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.ENCRYPT_MODE,
                createKey(password, salt),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce)
            )
        }
        return EncryptionResult(data = cipher.doFinal(rawData), iv = nonce)
    }

    override fun decrypt(
        encryptedData: ByteArray,
        iv: ByteArray,
        password: String,
        salt: String
    ): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                createKey(password, salt),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            )
        }
        return cipher.doFinal(encryptedData)
    }

    override fun generateSalt(): HashSalt {
        val bytes = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun hash(
        message: String,
        salt: String
    ): Pair<Hash, HashSalt> {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val result = argon2.hash(
            mode = ARGON2_MODE,
            password = message.toByteArray(Charsets.UTF_8),
            salt = saltBytes,
            tCostInIterations = TIME_COST,
            mCostInKibibyte = MEMORY_COST_KIB,
            parallelism = PARALLELISM,
            hashLengthInBytes = KEY_LENGTH_BYTES
        )
        return result.encodedOutputAsString() to salt
    }

    override fun areHashesMatch(value: String?, hash2: String?): Boolean {
        if (value == null || hash2 == null) return false
        return argon2.verify(
            mode = ARGON2_MODE,
            encoded = hash2,
            password = value.toByteArray(Charsets.UTF_8)
        )
    }
}