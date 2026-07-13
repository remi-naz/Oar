package dev.ridill.oar.transactions.domain.autoDetection

import android.telephony.SmsMessage
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.crashlytics.CrashlyticsManager
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.transactions.domain.model.Transaction
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TransactionAutoDetectService(
    private val extractor: TransactionDataExtractor,
    private val transactionRepo: TransactionRepository,
    private val crashlyticsManager: CrashlyticsManager,
    private val notificationHelper: NotificationHelper<Transaction>,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    fun detectTransactionsFromMessages(messages: List<SmsMessage>) {
        val messagesFromOrg = messages
            .filter { extractor.isOriginValidOrg(it.displayOriginatingAddress.orEmpty()) }
        val dateTimeNow = DateUtil.now()
        applicationScope.launch {
            for (message in messagesFromOrg) {
                try {
//                    if (extractor.isSupportedLanguage(message.messageBody))
//                        throw UnsupportedLanguageThrowable(message.messageBody)

                    val data = extractor.extractData(message.messageBody)
                    if (data.paymentTimestamp.isAfter(dateTimeNow)) continue

                    val insertedTx = transactionRepo.saveTransaction(
                        cycleId = OarDatabase.INVALID_ID_LONG,
                        amount = data.amount,
                        timestamp = data.paymentTimestamp,
                        note = data.note.orEmpty(),
                        type = data.fundMovement
                    )
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
                    crashlyticsManager.recordError(t)
                } catch (t: RegexUnavailableThrowable) {
                    crashlyticsManager.recordError(t)
                } catch (t: Throwable) {
                    t.rethrowIfCoroutineCancellation()
                    crashlyticsManager.recordError(t)
                }
            }
        }
    }
}