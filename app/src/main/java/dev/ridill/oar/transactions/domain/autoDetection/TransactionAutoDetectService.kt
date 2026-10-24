package dev.ridill.oar.transactions.domain.autoDetection

import android.telephony.SmsMessage
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.crashlytics.CrashlyticsManager
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.logW
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.transactions.domain.model.Transaction
import dev.ridill.oar.transactions.domain.repository.TransactionRepository

private const val TAG = "TransactionAutoDetect"

class TransactionAutoDetectService(
    private val extractor: TransactionDataExtractor,
    private val transactionRepo: TransactionRepository,
    private val crashlyticsManager: CrashlyticsManager,
    private val notificationHelper: NotificationHelper<Transaction>,
) {
    suspend fun detectTransactionsFromMessages(messages: List<SmsMessage>) {
        val messagesFromOrg = messages
            .filter { extractor.isOriginValidOrg(it.displayOriginatingAddress.orEmpty()) }
        val dateTimeNow = DateUtil.now()
        var savedCount = 0
        var futureDatedCount = 0
        var failedCount = 0
        for (message in messagesFromOrg) {
            try {
//                    if (extractor.isSupportedLanguage(message.messageBody))
//                        throw UnsupportedLanguageThrowable()

                val data = extractor.extractData(message.messageBody)
                if (data.paymentTimestamp.isAfter(dateTimeNow)) {
                    futureDatedCount++
                    continue
                }

                val insertedTx = transactionRepo.saveTransaction(
                    cycleId = OarDatabase.INVALID_ID_LONG,
                    amount = data.amount,
                    timestamp = data.paymentTimestamp,
                    note = data.note.orEmpty(),
                    type = data.fundMovement
                )
                savedCount++
                val parsedAmount = TextFormat.parseNumber(insertedTx.amount).orZero()

                notificationHelper.postNotification(
                    id = insertedTx.id.hashCode(),
                    data = insertedTx.copy(
                        amount = TextFormat.currency(
                            parsedAmount,
                            LocaleUtil.defaultCurrency
//                                currencyPrefRepo.getCurrencyPreferenceForMonth(dateTimeNow.toLocalDate())
//                                    .first()
                        )
                    )
                )
            } catch (t: TransactionDataExtractionFailedThrowable) {
                failedCount++
                logW(TAG) { "Extraction failed: ${t::class.simpleName}" }
                crashlyticsManager.recordError(t)
            } catch (t: RegexUnavailableThrowable) {
                failedCount++
                logW(TAG) { "Extraction patterns unavailable" }
                crashlyticsManager.recordError(t)
            } catch (t: Throwable) {
                t.rethrowIfCoroutineCancellation()
                failedCount++
                logW(TAG) { "Auto-detect failed: ${t::class.simpleName}" }
                crashlyticsManager.recordError(t)
            }
        }
        logI(TAG) {
            "Auto-detect done: received = ${messages.size}, fromOrg = ${messagesFromOrg.size}, " +
                    "saved = $savedCount, futureDated = $futureDatedCount, failed = $failedCount"
        }
    }
}