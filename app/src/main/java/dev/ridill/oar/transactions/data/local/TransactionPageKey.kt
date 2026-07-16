package dev.ridill.oar.transactions.data.local

import dev.ridill.oar.core.data.db.KeysetPageKey
import dev.ridill.oar.core.data.db.toSqliteUtcDateTimeString
import dev.ridill.oar.transactions.data.local.views.TransactionDetailsView

/**
 * Keyset cursor mirroring the paged list's ORDER BY
 * (cycleStartDate, cycleEndDate, transactionTimestamp, transactionId), all DESC.
 */
data class TransactionPageKey(
    val cycleStartDate: String,
    val cycleEndDate: String,
    val transactionTimestamp: String,
    val transactionId: Long
) : KeysetPageKey {
    override fun toValues(): List<Any> =
        listOf(cycleStartDate, cycleEndDate, transactionTimestamp, transactionId)
}

fun TransactionDetailsView.toPageKey(): TransactionPageKey = TransactionPageKey(
    cycleStartDate = cycleStartDate.toString(),
    cycleEndDate = cycleEndDate.toString(),
    transactionTimestamp = transactionTimestamp.toSqliteUtcDateTimeString(),
    transactionId = transactionId
)
