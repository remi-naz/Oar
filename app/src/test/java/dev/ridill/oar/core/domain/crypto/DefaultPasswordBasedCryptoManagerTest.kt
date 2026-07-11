package dev.ridill.oar.core.domain.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class DefaultPasswordBasedCryptoManagerTest {

    private lateinit var cryptoManager: PasswordBasedCryptoManager
    private lateinit var password: String

    @Before
    fun setUp() {
        cryptoManager = Argon2CryptoManager()
        password = "Test_Password"
    }

    @Test
    fun generateHashForSamePasswordTwice_hashesDifferent() {
        val (hash1, salt1) = cryptoManager.hash(password)
        println("hash1 = $hash1, salt1 = $salt1")
        val (hash2, salt2) = cryptoManager.hash(password)
        println("hash2 = $hash2, salt2 = $salt2")
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun checkPwWithSamePassword_returnsTrue() {
        val commonSalt = cryptoManager.generateSalt()
        println("commonSalt = $commonSalt")
        val (hash1, salt1) = cryptoManager.hash(password, commonSalt)
        println("hash1 = $hash1, salt1 = $salt1")
        val passwordToCheck = password
        val isPasswordMatch = cryptoManager.areHashesMatch(passwordToCheck, hash1)
        assertThat(isPasswordMatch).isTrue()
    }

    @Test
    fun checkPwWithDifferentPassword_returnsFalse() {
        val commonSalt = cryptoManager.generateSalt()
        println("commonSalt = $commonSalt")
        val (hash1, salt1) = cryptoManager.hash(password, commonSalt)
        println("hash1 = $hash1, salt1 = $salt1")
        val passwordToCheck = password + "_Diff"
        val (hash2, salt2) = cryptoManager.hash(passwordToCheck, commonSalt)
        println("hash2 = $hash2, salt2 = $salt2")
        val isPasswordMatch = cryptoManager.areHashesMatch(passwordToCheck, hash1)
        assertThat(isPasswordMatch).isFalse()
    }
}