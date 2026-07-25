package dev.ridill.oar.moneyPiles.data.local

import dev.ridill.oar.core.data.db.KeysetPagingSource
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileAggregateView
import kotlinx.coroutines.CoroutineScope

internal class MoneyPilesPagingSource(
    db: OarDatabase,
    applicationScope: CoroutineScope,
    private val query: String,
    private val dao: MoneyPileDao,
) : KeysetPagingSource<MoneyPilePageKey, MoneyPileAggregateView>(
    db = db,
    applicationScope = applicationScope,
    invalidationTables = setOf("money_pile_table", "money_pile_transactions_table")
) {
    override suspend fun fetch(
        cursor: MoneyPilePageKey?,
        direction: PageLoadDirection,
        loadSize: Int
    ): List<MoneyPileAggregateView> {
        val query = MoneyPilePagedQueryBuilder.build(
            query = query,
            cursor = cursor,
            direction = direction,
            limit = loadSize,
        )

        return dao.getMoneyPilesWithAggregatePagedRaw(query)
    }

    override fun keyOf(value: MoneyPileAggregateView): MoneyPilePageKey = value.toPageKey()
}