package dev.ridill.oar.core.domain.logging

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class LogRedactorTest {

    private lateinit var logRedactor: LogRedactor

    @Before
    fun setUp() {
        logRedactor = LogRedactor()
    }

    @Test
    fun bearerToken_isRedacted() {
        val result = logRedactor.redact("Authorization: Bearer ya29.a0AfH6SMB-xyz_123")
        assertThat(result).isEqualTo("Authorization: Bearer [REDACTED]")
    }

    @Test
    fun jwt_isRedacted() {
        val result = logRedactor.redact("idToken=eyJhbGciOi.eyJzdWIiOiIx.SflKxwRJSMeKKF2QT4")
        assertThat(result).isEqualTo("idToken=[TOKEN]")
    }

    @Test
    fun email_isRedacted() {
        val result = logRedactor.redact("Signed in as john.doe+test@gmail.com")
        assertThat(result).isEqualTo("Signed in as [EMAIL]")
    }

    @Test
    fun upiVpa_isRedacted() {
        val result = logRedactor.redact("Paid to merchant.store@okaxis")
        assertThat(result).isEqualTo("Paid to [VPA]")
    }

    @Test
    fun currencyAmounts_areRedacted() {
        assertThat(logRedactor.redact("Debited Rs.1,250.50 today")).isEqualTo("Debited [AMOUNT] today")
        assertThat(logRedactor.redact("Spent ₹ 300")).isEqualTo("Spent [AMOUNT]")
        assertThat(logRedactor.redact("INR 42 credited")).isEqualTo("[AMOUNT] credited")
    }

    @Test
    fun decimalNumbers_areRedacted() {
        val result = logRedactor.redact("aggregate = 1234.56")
        assertThat(result).isEqualTo("aggregate = [NUMBER]")
    }

    @Test
    fun longDigitRuns_areRedacted() {
        val result = logRedactor.redact("A/c XX123456 debited")
        assertThat(result).isEqualTo("A/c XX[NUMBER] debited")
    }

    @Test
    fun ordinaryMessage_isUnchanged() {
        val message = "cycle created with ID = 42 for 2026-10-24"
        assertThat(logRedactor.redact(message)).isEqualTo(message)
    }
}
