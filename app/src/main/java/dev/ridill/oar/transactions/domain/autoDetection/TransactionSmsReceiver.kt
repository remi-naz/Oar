package dev.ridill.oar.transactions.domain.autoDetection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.logW
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TransactionSmsReceiver"

@AndroidEntryPoint
class TransactionSmsReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var service: TransactionAutoDetectService

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            .ifEmpty { return }
            .toList()
        logI(TAG) { "SMS received, count = ${smsMessages.size}" }

        val pendingResult = goAsync()
        applicationScope.launch {
            try {
                service.detectTransactionsFromMessages(smsMessages)
            } catch (t: Throwable) {
                t.rethrowIfCoroutineCancellation()
                logW(TAG) { "SMS processing failed: ${t::class.simpleName}" }
            } finally {
                pendingResult.finish()
            }
        }
    }
}