package dev.ridill.oar.transactions.domain.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.transactions.domain.model.Transaction
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MarkTransactionExcludedActionReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var repo: TransactionRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<Transaction>

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ARG_TRANSACTION_ID, -1L)
        if (id < Long.Zero) return

        applicationScope.launch {
            repo.toggleExcluded(id, true)
            notificationHelper.dismissNotification(id.hashCode())
        }
    }
}