package dev.ridill.oar.transactions.domain.autoDetection

import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ridill.oar.core.domain.remoteConfig.FirebaseRemoteConfigService
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.WhiteSpace
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.ifNaN
import dev.ridill.oar.core.domain.util.joinToCapitalizedString
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.domain.model.FundMovement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * [TransactionDataExtractor] implementation using [Regex] pattern matching.
 *
 * This implementation is not my own
 * and was adopted from the [transaction-sms-parser](https://github.com/saurabhgupta050890/transaction-sms-parser) library
 * by user **saurabhgupta050890**. Checkout their GitHub page [here.](https://github.com/saurabhgupta050890)
 */

class RegexTransactionDataExtractor(
    @ApplicationContext private val applicationScope: CoroutineScope,
    private val remoteConfigService: FirebaseRemoteConfigService
) : TransactionDataExtractor {

    private val englishCharsRegex = ENGLISH_CHARS_PATTERN.toRegex()
    private lateinit var orgAddressRegex: Regex
    private lateinit var creditRegex: Regex
    private lateinit var debitRegex: Regex
    private lateinit var timestampRegex: Regex
    private lateinit var secondPartyStartRegex: Regex
    private lateinit var secondPartyEndRegex: Regex
    private lateinit var miscPaymentRegex: Regex

    init {
        applicationScope.launch {
            remoteConfigService.activate()
            val patterns = remoteConfigService.getConfig()
                .autoDetectTransactionRegexPatterns ?: return@launch
            orgAddressRegex = patterns.originatingAddress.toRegex()
            creditRegex = patterns.credit.toRegex()
            debitRegex = patterns.debit.toRegex()
            timestampRegex = patterns.timestamp.toRegex()
            secondPartyStartRegex = patterns.secondPartyStart.toRegex()
            secondPartyEndRegex = patterns.secondPartyEnd.toRegex()
            miscPaymentRegex = patterns.secondPartyStart.toRegex()
        }
    }

    override fun isSupportedLanguage(message: String): Boolean =
        englishCharsRegex.matchEntire(message) != null

    @Throws(RegexUnavailableThrowable::class)
    override fun isOriginValidOrg(originatingAddress: String): Boolean {
        if (::orgAddressRegex.isInitialized.not()) throw RegexUnavailableThrowable()
        return orgAddressRegex.matches(originatingAddress)
    }

    @Throws(
        AmountExtractionFailedThrowable::class,
        TransactionTypeExtractionFailedThrowable::class,
        TransactionNoteBuildFailedThrowable::class,
        TimestampExtractionFailedThrowable::class,
        TransactionDataExtractionFailedThrowable::class,
        RegexUnavailableThrowable::class
    )
    override fun extractData(messageBody: String): ExtractedTransactionData {
        val processedContent = processText(messageBody)

        val amount = extractTransactionAmount(processedContent)
        val transactionType = extractTransactionType(processedContent)
        logD { "Extracted type - $transactionType" }
        val note = buildNote(processedContent, transactionType)
        val timestamp = extractTimestamp(messageBody)
        logD { "Extracted timestamp - $timestamp" }

        return ExtractedTransactionData(
            amount = amount,
            paymentTimestamp = timestamp,
            fundMovement = transactionType,
            note = note
        )
    }

    @Throws(
        AmountExtractionFailedThrowable::class,
        RegexUnavailableThrowable::class
    )
    private fun extractTransactionAmount(content: List<String>): Double {
        val index = content.indexOfFirst { it.startsWith("rs.") }
            .takeIf { it > -1 }
            ?: throw AmountExtractionFailedThrowable("Failed to find 'rs' keyword in ${content.size} tokens")

        val amountString = content[index + 1]
        val amount = amountString
            .removePrefix("rs.")
            .replace(",", String.Empty)
            .let(TextFormat::parseNumber)
            ?: throw AmountExtractionFailedThrowable("Failed parsing amount string to double")

        return amount.ifNaN { Double.Zero }
    }

    @Throws(
        TransactionNoteBuildFailedThrowable::class,
        RegexUnavailableThrowable::class
    )
    private fun buildNote(content: List<String>, type: FundMovement): String {
        if (
            ::secondPartyStartRegex.isInitialized.not()
            || ::secondPartyEndRegex.isInitialized.not()
        ) throw RegexUnavailableThrowable()

        val joinedString = content.joinToString(String.WhiteSpace)
        var secondParty: String? = null
        if ("vpa" in content) {
            val index = content.indexOf("vpa")
            if (index < content.lastIndex) {
                val nextStr = content[index + 1]
                secondParty = nextStr
            }
        }

        /*var match = ""
        upiKeywords.forEach { keyword ->
            if (joinedString.indexOf(keyword) > 0) {
                match = keyword
            }
        }

        if (match.isNotEmpty()) {
            val nextWord = getNextWord(joinedString, match)

            if (secondParty.isNullOrEmpty()) secondParty = nextWord
        }*/

        if (secondParty.isNullOrEmpty()) {
            val secondPartyStartIndex = content.indexOfFirst { it.matches(secondPartyStartRegex) }
                .takeIf { it > -1 }
                ?: throw TransactionNoteBuildFailedThrowable("Failed to find start index of second party keyword")

            val secondPartyEndIndex = content
                .subList(secondPartyStartIndex + 1, content.size)
                .indexOfFirst { it.matches(secondPartyEndRegex) }
                .takeIf { it > -1 }
                ?: throw TransactionNoteBuildFailedThrowable("Failed to find ending index of second party")

            secondParty = content
                .subList(secondPartyStartIndex + 1, secondPartyStartIndex + secondPartyEndIndex + 1)
                .joinToCapitalizedString()
        }

        return buildString {
            when (type) {
                FundMovement.IN -> append("From")
                FundMovement.OUT -> append("Towards")
            }

            append(String.WhiteSpace)

            append(secondParty.trim('.', ',', ' ', '-', '_'))
        }
    }

    @Throws(
        TransactionTypeExtractionFailedThrowable::class,
        RegexUnavailableThrowable::class
    )
    private fun extractTransactionType(content: List<String>): FundMovement {
        if (
            ::debitRegex.isInitialized.not()
            || ::miscPaymentRegex.isInitialized.not()
            || ::creditRegex.isInitialized.not()
        ) throw RegexUnavailableThrowable()
        val contentString = content.joinToString(String.WhiteSpace)

        return when (true) {
            contentString.contains(debitRegex) -> FundMovement.OUT
            contentString.contains(miscPaymentRegex) -> FundMovement.OUT
            contentString.contains(creditRegex) -> FundMovement.IN
            else -> throw TransactionTypeExtractionFailedThrowable("Failed to find type match")
        }
    }

    @Throws(
        TimestampExtractionFailedThrowable::class,
        RegexUnavailableThrowable::class
    )
    private fun extractTimestamp(messageBody: String): LocalDateTime {
        if (::timestampRegex.isInitialized.not()) throw RegexUnavailableThrowable()


        val timestampMatchGroups = timestampRegex.find(messageBody)?.groupValues
        logD { "Timestamp match groups - $timestampMatchGroups" }
        if (timestampMatchGroups.isNullOrEmpty())
            throw TimestampExtractionFailedThrowable("Failed to find timestamp match groups")

        val dateString = timestampMatchGroups.getOrNull(1)
            ?: throw TimestampExtractionFailedThrowable("Failed to get date string from ${timestampMatchGroups.size} match groups")
        logD { "Date string - $dateString" }
        val timeString = timestampMatchGroups.getOrNull(3).orEmpty()
        logD { "Time string - $timeString" }
        val dateTimeNow = DateUtil.now()
        val dateTimeFormatterBuilder = DateTimeFormatterBuilder().apply {
            // Build date section
            val dateDelimiter = dateString.find { !it.isDigit() }
                ?: throw TimestampExtractionFailedThrowable("Failed to find date delimiter in date string")
            val dateParts = dateString.split(dateDelimiter)
            val isFirstPartYear = dateParts[0].length == 4
            dateParts.forEachIndexed { index, part ->
                when (index) {
                    0 -> {
                        if (isFirstPartYear) {
                            val pattern = part.map { 'y' }.joinToString(String.Empty)
                            appendPattern(pattern)
                        } else {
                            val pattern = part.map { 'd' }.joinToString(String.Empty)
                            appendPattern(pattern)
                        }
                    }

                    2 -> {
                        appendPattern(dateDelimiter.toString())
                        if (isFirstPartYear) {
                            val pattern = part.map { 'd' }.joinToString(String.Empty)
                            appendPattern(pattern)
                        } else {
                            optionalStart()
                            appendPattern("yyyy")
                            optionalEnd()
                            optionalStart()
                            appendValueReduced(ChronoField.YEAR, 2, 2, 1920)
                            optionalEnd()
                        }
                    }

                    else -> {
                        appendPattern(dateDelimiter.toString())
                        val pattern = part.map { 'M' }.joinToString(String.Empty)
                        appendPattern(pattern)
                    }
                }
            }

            if (timeString.isNotEmpty()) {
                // Built time section
                val timeDelimiter = timeString.find { !it.isDigit() }
                    ?: throw TimestampExtractionFailedThrowable("Failed to find time delimiter in time string")
                val timeParts = timeString.split(timeDelimiter)
                if (timeParts.isNotEmpty()) appendPattern(":")
                timeParts.forEachIndexed { index, part ->
                    when (index) {
                        0 -> {
                            appendPattern(part.map { 'H' }.joinToString(String.Empty))
                        }

                        1 -> {
                            appendPattern(timeDelimiter.toString())
                            appendPattern(part.map { 'm' }.joinToString(String.Empty))
                        }

                        else -> {
                            appendPattern(timeDelimiter.toString())
                            appendPattern(part.map { 's' }.joinToString(String.Empty))
                        }
                    }
                }
            }

            parseDefaulting(ChronoField.YEAR, dateTimeNow.year.toLong())
            parseDefaulting(ChronoField.MONTH_OF_YEAR, dateTimeNow.monthValue.toLong())
            parseDefaulting(ChronoField.DAY_OF_MONTH, dateTimeNow.dayOfMonth.toLong())
            parseDefaulting(ChronoField.HOUR_OF_DAY, dateTimeNow.hour.toLong())
            parseDefaulting(ChronoField.MINUTE_OF_HOUR, dateTimeNow.minute.toLong())
            parseDefaulting(ChronoField.SECOND_OF_MINUTE, dateTimeNow.second.toLong())
        }

        val timestampString = "$dateString${timeString}".trim()
        logD { "Timestamp string - $timestampString" }

        return try {
            DateUtil.parseDateTime(
                value = timestampString,
                formatter = dateTimeFormatterBuilder.toFormatter()
            )
        } catch (t: Throwable) {
            throw TransactionDataExtractionFailedThrowable("Timestamp parse failed | pattern - ${dateTimeFormatterBuilder.toFormatter()} | cause - ${t::class.simpleName}")
        }
    }

    /*private fun getNextWord(source: String, searchWord: String, count: Int = 1): String {
        val splits = source.split(searchWord, ignoreCase = true, limit = 2)
        splits.getOrNull(1)?.let { nextGroup ->
            val workSplitRegex = "/[^0-9a-zA-Z]+/gi".toRegex()
            return nextGroup.trim().split(workSplitRegex, count).joinToString(String.WhiteSpace)
        }

        return String.Empty
    }*/

    private fun processText(string: String): List<String> {
        var message = string.lowercase(LocaleUtil.defaultLocale)
        // remove '-'
        message = message.replace("-".toRegex(), String.Empty)
        // remove '!'
        message = message.replace("!".toRegex(), "")
        // remove ':'
        message = message.replace(":".toRegex(), " ")
        // remove '/'
        message = message.replace("/".toRegex(), "")
        // remove '='
        message = message.replace("=".toRegex(), " ")
        // remove '{}'
        message = message.replace("[{}]".toRegex(), " ")
        // remove \n
        message = message.replace("\n".toRegex(), " ")
        // remove \r
        message = message.replace("\r".toRegex(), " ")
        // remove 'ending'
        message = message.replace("ending ".toRegex(), "")
        // replace 'x'
        message = message.replace("x|[*]".toRegex(), "")
        // // remove 'is' 'with'
        // message = message.replace(\bis\b|\bwith\b, '')
        // replace 'is'
        message = message.replace("(?i)is ".toRegex(), "")
        // replace 'with'
        message = message.replace("(?i)with ".toRegex(), "")
        // remove 'no.'
        message = message.replace("(?i)no. ".toRegex(), "")
        // replace all ac, acct, account with ac
        message = message.replace("(?i)\bac\b|\bacct\b|\baccount\b".toRegex(), "ac")
        // replace all 'rs' with 'rs. '
        message = message.replace("(?i)rs(?=\\w)".toRegex(), "rs. ")
        // replace all 'rs ' with 'rs. '
        message = message.replace("(?i)rs ".toRegex(), "rs. ")
        // replace all inr with rs.
        message = message.replace("(?i)inr(?=\\w)".toRegex(), "rs. ")
        //
        message = message.replace("(?i)inr ".toRegex(), "rs. ")
        // replace all 'rs. ' with 'rs.'
        message = message.replace("(?i)rs. ".toRegex(), "rs.")
        // replace all 'rs.' with 'rs. '
        message = message.replace("(?i)rs.(?=\\w)".toRegex(), "rs. ")
        // replace all 'debited' with ' debited '
        message = message.replace("(?i)debited".toRegex(), " debited ")
        // replace all 'credited' with ' credited '
        message = message.replace("(?i)credited".toRegex(), " credited ")
        // combine words
        /*combinedWords.forEach((word) => {
            message = message.replace(word.regex, word.word);
        })*/
        return message.split(" ").filter { it.isNotEmpty() }
    }

    /*companion object {
        val upiKeywords: List<String>
            get() = listOf("upi", "ref no", "upi ref", "upi ref no")
    }*/
}

private const val ENGLISH_CHARS_PATTERN = "(?i)^[a-zA-Z0-9 !@#$%^&*()-_+=/?\n]*\$"

class RegexUnavailableThrowable : Throwable()

class UnsupportedLanguageThrowable :
    TransactionDataExtractionFailedThrowable("UnsupportedLanguageThrowable:Message is not supported language")

class AmountExtractionFailedThrowable(message: String) :
    TransactionDataExtractionFailedThrowable("AmountExtractionThrowable:\n$message")

class TransactionTypeExtractionFailedThrowable(message: String) :
    TransactionDataExtractionFailedThrowable("TransactionTypeExtractionFailedThrowable:\n$message")

class TransactionNoteBuildFailedThrowable(message: String) :
    TransactionDataExtractionFailedThrowable("TransactionNoteBuildFailedThrowable:\n$message")

class TimestampExtractionFailedThrowable(message: String) :
    TransactionDataExtractionFailedThrowable("TimestampExtractionFailedThrowable:\n$message")