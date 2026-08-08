package dev.ridill.oar.transactions.domain.autoDetection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

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

        val pendingResult = goAsync()
        applicationScope.launch {
            try {
                service.detectTransactionsFromMessages(smsMessages)
            } finally {
                pendingResult.finish()
            }
        }
    }
}