package dev.ridill.oar.moneyPiles.data.local

import dev.ridill.oar.core.data.db.KeysetPageKey
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileAggregateView

data class MoneyPilePageKey(
    val completed: Long,
    val id: Long
) : KeysetPageKey {
    override fun toValues(): List<Any> = listOf(completed, id)
}

fun MoneyPileAggregateView.toPageKey(): MoneyPilePageKey = MoneyPilePageKey(
    completed = if (completionTimestamp != null) 1L else 0L,
    id = id
)
