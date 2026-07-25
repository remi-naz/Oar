package dev.ridill.oar.moneyPiles.data.local

import dev.ridill.oar.core.data.db.KeysetPagingSource
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import kotlinx.coroutines.CoroutineScope

class PileTransactionPagingSource(
    db: OarDatabase,
    applicationScope: CoroutineScope,
    private val pileId: Long,
    private val dao: MoneyPileDao,
) : KeysetPagingSource<PileTransactionKey, MoneyPileTransactionsEntity>(
    db = db,
    applicationScope = applicationScope,
    invalidationTables = setOf("money_pile_transactions_table")
) {
    override suspend fun fetch(
        cursor: PileTransactionKey?,
        direction: PageLoadDirection,
        loadSize: Int
    ): List<MoneyPileTransactionsEntity> {
        val query = PileTransactionPagedQueryBuilder.build(
            pileId = pileId,
            cursor = cursor,
            direction = direction,
            limit = loadSize,
        )

        return dao.getTransactionsInPilePagedRaw(query)
    }

    override fun keyOf(value: MoneyPileTransactionsEntity): PileTransactionKey = value.toKey()
}