package dev.ridill.oar.core.domain.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Instrumented because Argon2Kt loads a native library that only exists for Android ABIs, so
 * it cannot run as a host JVM unit test.
 */
class Argon2CryptoManagerTest {

    private lateinit var cryptoManager: PasswordBasedCryptoManager
    private val password = "Test_Password"

    @Before
    fun setUp() {
        cryptoManager = Argon2CryptoManager()
    }

    @Test
    fun hashingSamePasswordTwice_producesDifferentHashes() {
        val (hash1, _) = cryptoManager.hash(password)
        val (hash2, _) = cryptoManager.hash(password)

        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun areHashesMatch_withSamePassword_returnsTrue() {
        val (hash, _) = cryptoManager.hash(password, cryptoManager.generateSalt())

        assertThat(cryptoManager.areHashesMatch(password, hash)).isTrue()
    }

    @Test
    fun areHashesMatch_withDifferentPassword_returnsFalse() {
        val (hash, _) = cryptoManager.hash(password, cryptoManager.generateSalt())

        assertThat(cryptoManager.areHashesMatch("${password}_Diff", hash)).isFalse()
    }
}
