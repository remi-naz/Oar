package dev.ridill.oar.moneyPiles.data.local

import dev.ridill.oar.core.data.db.KeysetPageKey
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileAggregateView

data class MoneyPilePageKey(
    val id: Long
) : KeysetPageKey {
    override fun toValues(): List<Any> = listOf(id)
}

fun MoneyPileAggregateView.toPageKey(): MoneyPilePageKey = MoneyPilePageKey(
    id = id
)
