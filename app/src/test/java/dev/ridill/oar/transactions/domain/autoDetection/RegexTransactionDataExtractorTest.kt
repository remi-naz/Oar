package dev.ridill.oar.transactions.domain.autoDetection

import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.remoteConfig.AutoDetectTransactionRegexPatterns
import dev.ridill.oar.fakes.FakeRemoteConfigService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RegexTransactionDataExtractorTest {

    // The production patterns are served by remote config, so these are fixtures shaped to the
    // sample bank messages below; each field gets its own distinct pattern.
    private val patterns = AutoDetectTransactionRegexPatterns(
        originatingAddress = "[A-Z]{2}-HDFC[A-Z]{2}",
        credit = "\\bcredited\\b",
        debit = "\\bsent\\b",
        timestamp = "(\\d{2}[-/]\\d{2}(?:[-/]\\d{2,4})?)( (\\d{2}:\\d{2}(?::\\d{2})?))?",
        secondPartyStart = "to",
        secondPartyEnd = "on",
        miscPayment = "\\bpaid\\b"
    )

    private fun extractor(
        patterns: AutoDetectTransactionRegexPatterns? = this.patterns,
        remoteConfig: FakeRemoteConfigService = FakeRemoteConfigService(
            FakeRemoteConfigService.defaultConfig(patterns)
        )
    ): TransactionDataExtractor = RegexTransactionDataExtractor(
        // Unconfined so the pattern load in init has finished before the first assertion
        applicationScope = CoroutineScope(UnconfinedTestDispatcher()),
        remoteConfigService = remoteConfig
    )

    @Test
    fun activatesRemoteConfigBeforeReadingPatterns() {
        val remoteConfig = FakeRemoteConfigService(FakeRemoteConfigService.defaultConfig(patterns))

        extractor(remoteConfig = remoteConfig)

        assertThat(remoteConfig.activateCount).isEqualTo(1)
    }

    @Test
    fun validOriginAddress_returnsTrue() {
        assertThat(extractor().isOriginValidOrg("AX-HDFCBX")).isTrue()
    }

    @Test
    fun invalidOriginAddress_returnsFalse() {
        assertThat(extractor().isOriginValidOrg("9846631098")).isFalse()
    }

    @Test
    fun noPatternsInRemoteConfig_originCheckThrowsRegexUnavailable() {
        val extractor = extractor(patterns = null)

        val result = runCatching { extractor.isOriginValidOrg("AX-HDFCBX") }

        assertThat(result.exceptionOrNull()).isInstanceOf(RegexUnavailableThrowable::class.java)
    }

    @Test
    fun noPatternsInRemoteConfig_extractDataThrowsRegexUnavailable() {
        val extractor = extractor(patterns = null)

        val result = runCatching { extractor.extractData(DEBIT_WITHOUT_YEAR) }

        assertThat(result.exceptionOrNull()).isInstanceOf(RegexUnavailableThrowable::class.java)
    }

    @Test
    fun englishMessage_isSupportedLanguage() {
        assertThat(extractor().isSupportedLanguage("Hello")).isTrue()
    }

    @Test
    fun malayalamMessage_isNotSupportedLanguage() {
        assertThat(extractor().isSupportedLanguage("ഹലോ")).isFalse()
    }

    @Test
    fun allSampleMessages_areSupportedLanguage() {
        val extractor = extractor()

        ALL_SAMPLES.forEach { message ->
            assertThat(extractor.isSupportedLanguage(message)).isTrue()
        }
    }

    @Test
    fun debit_withoutYear_extractsAmountMovementPartyAndDayMonth() {
        val data = extractor().extractData(DEBIT_WITHOUT_YEAR)

        assertThat(data.amount).isEqualTo(250.0)
        assertThat(data.fundMovement).isEqualTo(FundMovement.OUT)
        assertThat(data.note).isEqualTo("Towards H R Fruits")
        // The year is missing from the message, so it falls back to the current one
        assertThat(data.paymentTimestamp.dayOfMonth).isEqualTo(27)
        assertThat(data.paymentTimestamp.monthValue).isEqualTo(3)
    }

    @Test
    fun debit_withLargerAmount_extractsAmountAndParty() {
        val data = extractor().extractData(DEBIT_LARGER_AMOUNT)

        assertThat(data.amount).isEqualTo(3000.0)
        assertThat(data.fundMovement).isEqualTo(FundMovement.OUT)
        assertThat(data.note).isEqualTo("Towards Marvin Clement")
    }

    @Ignore("Known bug 1 + 2: RegexTransactionDataExtractor.init assigns miscPaymentRegex from patterns.secondPartyStart, so any message that matches the second-party-start pattern is classified OUT; the 2-digit year then hits: RegexTransactionDataExtractor builds 2-digit years with appendValueReduced(YEAR, 2, 2, 1920), so 03-04-20 parses as 1920-04-03 and 28-03-24 as 1924-03-28")
    @Test
    fun credit_viaVpa_isFundMovementInFromTheVpa() {
        val data = extractor().extractData(CREDIT_VIA_VPA)

        assertThat(data.amount).isEqualTo(90.0)
        assertThat(data.fundMovement).isEqualTo(FundMovement.IN)
        assertThat(data.note).isEqualTo("From clementmarvin051@okicici")
        assertThat(data.paymentTimestamp.toLocalDate()).isEqualTo(LocalDate.of(2024, 3, 28))
    }

    @Ignore("Known bug: RegexTransactionDataExtractor builds 2-digit years with appendValueReduced(YEAR, 2, 2, 1920), so 03-04-20 parses as 1920-04-03 and 28-03-24 as 1924-03-28")
    @Test
    fun debit_withTwoDigitYear_parsesFullDate() {
        val data = extractor().extractData(DEBIT_WITH_SHORT_YEAR)

        assertThat(data.fundMovement).isEqualTo(FundMovement.OUT)
        assertThat(data.paymentTimestamp.toLocalDate()).isEqualTo(LocalDate.of(2020, 4, 3))
    }

    @Test
    fun messageWithoutAnyAmount_throwsAmountExtractionFailed() {
        val result = runCatching { extractor().extractData("HDFC Bank: nothing to see here on 28-03-24") }

        assertThat(result.exceptionOrNull()).isInstanceOf(AmountExtractionFailedThrowable::class.java)
    }

    @Ignore("Known bug: RegexTransactionDataExtractor.init assigns miscPaymentRegex from patterns.secondPartyStart, so any message that matches the second-party-start pattern is classified OUT")
    @Test
    fun messageThatIsNeitherDebitNorCredit_throwsTypeExtractionFailed() {
        val result = runCatching {
            extractor().extractData("HDFC Bank: Rs. 90.00 was noted on 28-03-24 by a/c linked to VPA friend@bank")
        }

        assertThat(result.exceptionOrNull())
            .isInstanceOf(TransactionTypeExtractionFailedThrowable::class.java)
    }

    private companion object {
        val DEBIT_WITHOUT_YEAR = """
            Amt Sent Rs.250.00
            From HDFC Bank A/C *1643
            To H R FRUITS
            On 27-03
            Ref 408718670922
            Not You? Call 18002586161/SMS BLOCK UPI to 7308080808
        """.trimIndent()

        val DEBIT_LARGER_AMOUNT = """
            Amt Sent Rs.3000.00
            From HDFC Bank A/C *1643
            To MARVIN CLEMENT
            On 28-03
            Ref 408856589439
            Not You? Call 18002586161/SMS BLOCK UPI to 7308080808
        """.trimIndent()

        const val CREDIT_VIA_VPA =
            "HDFC Bank: Rs. 90.00 credited to a/c XXXXXX1643 on 28-03-24 by a/c linked to VPA " +
                    "clementmarvin05-1@okicici (UPI Ref No  408837405314)."

        val DEBIT_WITH_SHORT_YEAR = """
            Amt Sent Rs.20.00
            From HDFC Bank A/C *1643
            To MARVIN CLEMENT
            On 03-04-20
            Ref 408856589439
            Not You? Call 18002586161/SMS BLOCK UPI to 7308080808
        """.trimIndent()

        val ALL_SAMPLES = listOf(
            DEBIT_WITHOUT_YEAR,
            DEBIT_LARGER_AMOUNT,
            CREDIT_VIA_VPA,
            DEBIT_WITH_SHORT_YEAR
        )
    }
}
