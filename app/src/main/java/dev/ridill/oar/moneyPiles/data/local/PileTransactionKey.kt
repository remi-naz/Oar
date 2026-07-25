package dev.ridill.oar.moneyPiles.data.local

import dev.ridill.oar.core.data.db.KeysetPageKey
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity

class PileTransactionKey(
    val id: Long,
) : KeysetPageKey {
    override fun toValues(): List<Any> = listOf(id)
}

fun MoneyPileTransactionsEntity.toKey() = PileTransactionKey(id)