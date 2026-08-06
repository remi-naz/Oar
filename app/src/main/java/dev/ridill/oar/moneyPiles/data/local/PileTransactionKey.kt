package dev.ridill.oar.moneyPiles.data.local

import dev.ridill.oar.core.data.db.KeysetPageKey
import dev.ridill.oar.core.data.db.toSqliteUtcDateTimeString
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity

class PileTransactionKey(
    val createdTimestamp: String,
    val id: Long,
) : KeysetPageKey {
    override fun toValues(): List<Any> = listOf(createdTimestamp, id)
}

fun MoneyPileTransactionsEntity.toKey() = PileTransactionKey(
    createdTimestamp = createdTimestamp.toSqliteUtcDateTimeString(),
    id = id
)